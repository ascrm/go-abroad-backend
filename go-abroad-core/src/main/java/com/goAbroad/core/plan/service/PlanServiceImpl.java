package com.goAbroad.core.plan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.common.result.PageR;
import com.goAbroad.core.plan.dto.*;
import com.goAbroad.core.plan.entity.Plan;
import com.goAbroad.core.plan.entity.PlanPhase;
import com.goAbroad.core.plan.entity.PlanTask;
import com.goAbroad.core.plan.mapper.PlanMapper;
import com.goAbroad.core.plan.repository.PlanPhaseRepository;
import com.goAbroad.core.plan.repository.PlanRepository;
import com.goAbroad.core.plan.repository.PlanTaskRepository;
import com.goAbroad.core.plan.utils.AiUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl {

    private final PlanRepository planRepository;
    private final PlanPhaseRepository phaseRepository;
    private final PlanTaskRepository taskRepository;
    private final PlanMapper planMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    // Redis key 前缀
    private static final String PLAN_PARSED_KEY_PREFIX = "plan:parsed:";

    public PageR<PlanResponse> getPlanList(Long userId, String type, String status, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        Page<Plan> planPage = planRepository.findByUserIdWithFilters(userId, type, status, pageRequest);

        List<PlanResponse> list = planMapper.toResponseList(planPage.getContent());

        return PageR.ok(planPage.getTotalElements(), list, page, pageSize);
    }

    public PlanDetailResponse getPlanDetail(Long userId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该规划");
        }

        List<PlanPhase> phases = phaseRepository.findByPlanIdOrderBySortOrder(planId);
        List<PhaseResponse> phaseResponses = phases.stream()
                .map(phase -> {
                    PhaseResponse response = planMapper.toPhaseResponse(phase);
                    List<PlanTask> tasks = taskRepository.findByPhaseIdOrderBySortOrder(phase.getId());
                    response.setTasks(planMapper.toTaskResponseList(tasks));
                    return response;
                })
                .collect(Collectors.toList());

        PlanDetailResponse detailResponse = planMapper.toDetailResponse(plan);
        detailResponse.setPhases(phaseResponses);
        return detailResponse;
    }

    @Transactional
    public PlanResponse createPlan(Long userId, PlanCreateRequest request) {
        Plan plan = planMapper.toEntity(request);
        plan.setUserId(userId);
        plan = planRepository.save(plan);
        return planMapper.toResponse(plan);
    }

    @Transactional
    public PlanResponse updatePlan(Long userId, Long planId, PlanUpdateRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该规划");
        }

        planMapper.updateFromRequest(request, plan);
        plan = planRepository.save(plan);
        return planMapper.toResponse(plan);
    }

    @Transactional
    public void deletePlan(Long userId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该规划");
        }

        List<PlanPhase> phases = phaseRepository.findByPlanIdOrderBySortOrder(planId);
        for (PlanPhase phase : phases) {
            taskRepository.deleteByPhaseId(phase.getId());
        }
        phaseRepository.deleteByPlanId(planId);
        planRepository.delete(plan);
    }

    /**
     * 流式生成规划 - 使用SseEmitter返回AI流式响应
     * 流式完成后自动同步解析 JSON 并存入 Redis
     */
    public SseEmitter generatePlanStream(Long userId, GeneratePlanRequest request) {

        String prompt = AiUtils.buildPrompt(request);
        String systemPrompt = AiUtils.getSystemPromptByType(request.getType());
        SseEmitter emitter = new SseEmitter(300_000L);
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        StringBuilder contentBuilder = new StringBuilder();
        String parseKey = java.util.UUID.randomUUID().toString();

        // 统一的清理方法
        Runnable cleanup = () -> {
            if (isCompleted.compareAndSet(false, true)) {
                emitter.complete();
            }
        };

        // 注册回调
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> {log.warn("SSE 错误: {}", e.getMessage());cleanup.run();});

        // 使用SseEmitter处理异步流式响应
        chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .stream()
                .content()
                .subscribe(
                        chunk -> {
                            if (isCompleted.get()) return; // 如果已断开，直接跳过
                            contentBuilder.append(chunk);
                            try {
                                emitter.send(SseEmitter.event().data(chunk));
                            } catch (Exception ignore) {}
                        },
                        error -> {
                            log.error("AI 响应错误: {}", error.getMessage());
                            cleanup.run();
                        },
                        () -> {
                            if (!isCompleted.get()) {
                                try {
                                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                                } catch (Exception ignored) {}

                                String fullContent = contentBuilder.toString();
                                parseAndSaveToRedis(parseKey, fullContent);
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("parseKey")
                                            .data("parseKey:" + parseKey));
                                } catch (Exception ignored) {}
                                cleanup.run();
                            }
                        }
                );

        return emitter;
    }

    /**
     * 同步解析 content 为 JSON 并存入 Redis
     */
    @SneakyThrows
    private void parseAndSaveToRedis(String parseKey, String content) {
        String parsePrompt = AiUtils.buildParseContentPrompt(content);
        String aiResponse = chatClient.prompt()
                .system("你是一个专业的JSON解析器，请准确解析文本为JSON格式。只返回JSON，不要其他内容。")
                .user(parsePrompt)
                .call()
                .content();

        SaveGeneratedRequest.ParsedContent parsed = parseAiResponse(aiResponse);
        String json = objectMapper.writeValueAsString(parsed);
        redisTemplate.opsForValue().set(PLAN_PARSED_KEY_PREFIX + parseKey, json, Duration.ofMinutes(10));
    }

    /**
     * 解析 AI 返回的 JSON 响应
     */
    private SaveGeneratedRequest.ParsedContent parseAiResponse(String aiResponse) {
        try {
            // 去除 markdown 代码块标记
            String jsonStr = aiResponse
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "")
                    .trim();

            return objectMapper.readValue(jsonStr, SaveGeneratedRequest.ParsedContent.class);
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());
            throw new BusinessException("解析规划内容失败，请重试");
        }
    }


    /**
     * 获取redis中的JSON数据
     * 优先从 Redis 获取，如果没有则降级调用 AI 解析
     */
    @SneakyThrows
    private SaveGeneratedRequest.ParsedContent getParsedContent(SaveGeneratedRequest request) {
        String parseKey = request.getParseKey();
        String json = redisTemplate.opsForValue().get(PLAN_PARSED_KEY_PREFIX + parseKey);
        redisTemplate.delete(PLAN_PARSED_KEY_PREFIX + parseKey);
        return objectMapper.readValue(json, SaveGeneratedRequest.ParsedContent.class);
    }

    @Transactional
    public PlanResponse saveGeneratedPlan(Long userId, SaveGeneratedRequest request) {
        // 1. 从 Redis 获取已解析的 JSON
        SaveGeneratedRequest.ParsedContent parsed = getParsedContent(request);

        // 2. 保存规划
        Plan plan = Plan.builder()
                .userId(userId)
                .title(parsed.getTitle())
                .type(Plan.PlanType.valueOf(request.getType()))
                .destination(request.getDestination())
                .formData(request.getFormData())
                .status("completed")
                .build();
        plan = planRepository.save(plan);

        // 4. 批量保存阶段
        List<PlanPhase> phases = new ArrayList<>();
        if (parsed.getPhases() != null) {
            int phaseOrder = 0;
            for (SaveGeneratedRequest.PhaseDto phaseDto : parsed.getPhases()) {
                PlanPhase phase = PlanPhase.builder()
                        .planId(plan.getId())
                        .title(phaseDto.getTitle())
                        .description(phaseDto.getDescription())
                        .sortOrder(phaseOrder++)
                        .build();
                phases.add(phase);
            }
            phases = phaseRepository.saveAll(phases);
        }

        // 5. 批量保存任务
        List<PlanTask> tasks = new ArrayList<>();
        for (int i = 0; i < phases.size(); i++) {
            PlanPhase phase = phases.get(i);
            SaveGeneratedRequest.PhaseDto phaseDto = parsed.getPhases().get(i);
            if (phaseDto.getTasks() != null) {
                int taskOrder = 0;
                for (SaveGeneratedRequest.TaskDto taskDto : phaseDto.getTasks()) {
                    PlanTask task = PlanTask.builder()
                            .phaseId(phase.getId())
                            .title(taskDto.getTitle())
                            .description(taskDto.getDescription())
                            .sortOrder(taskOrder++)
                            .isCompleted(false)
                            .build();
                    tasks.add(task);
                }
            }
        }
        taskRepository.saveAll(tasks);

        return planMapper.toResponse(plan);
    }

    @Transactional
    public void reorderPhases(Long userId, Long planId, ReorderRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该规划");
        }

        List<Long> phaseIds = request.getPhaseIds();
        for (int i = 0; i < phaseIds.size(); i++) {
            final int finalI = i;
            phaseRepository.findById(phaseIds.get(i)).ifPresent(phase -> {
                phase.setSortOrder(finalI);
                phaseRepository.save(phase);
            });
        }
    }
}
