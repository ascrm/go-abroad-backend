package com.goAbroad.core.plan.service;

import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.common.result.PageR;
import com.goAbroad.core.plan.dto.*;
import com.goAbroad.core.plan.entity.Plan;
import com.goAbroad.core.plan.entity.PlanPhase;
import com.goAbroad.core.plan.entity.PlanTask;
import com.goAbroad.core.plan.enums.PlanStatus;
import com.goAbroad.core.plan.enums.TaskStatus;
import com.goAbroad.core.plan.mapper.PlanMapper;
import com.goAbroad.core.plan.repository.PlanPhaseRepository;
import com.goAbroad.core.plan.repository.PlanRepository;
import com.goAbroad.core.plan.repository.PlanTaskRepository;
import com.goAbroad.core.plan.utils.AiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl {

    private final PlanRepository planRepository;
    private final PlanPhaseRepository phaseRepository;
    private final PlanTaskRepository taskRepository;
    private final PlanMapper planMapper;
    private final ChatClient chatClient;

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

        PlanStatus newStatus = PlanStatus.valueOf(request.getStatus());
        if (newStatus == PlanStatus.generating) {
            boolean hasGenerating = planRepository
                    .findByUserIdAndStatusAndIsDeletedFalse(userId, PlanStatus.generating)
                    .filter(p -> !p.getId().equals(planId))
                    .isPresent();
            if (hasGenerating) {
                throw new BusinessException("当前存在进行中的规划...");
            }
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

        // 软删除：将 is_deleted 设置为 true
        plan.setIsDeleted(true);
        planRepository.save(plan);
    }

    /**
     * 流式生成规划 - 使用SseEmitter返回AI流式响应
     */
    public SseEmitter generatePlanStream(Long userId, GeneratePlanRequest request) {
        String prompt = AiUtils.buildPrompt(request);
        String systemPrompt = AiUtils.getSystemPromptByType(request.getType());
        SseEmitter emitter = new SseEmitter(300_000L);
        emitter.onCompletion(emitter::complete);
        emitter.onTimeout(emitter::complete);

        // 使用SseEmitter处理异步流式响应
        chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .stream()
                .content()
                .doOnError(e -> log.error("AI响应错误", e))
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event().data(chunk));
                            } catch (Exception e) {throw new RuntimeException("SSE send failed");
                            }
                        },
                        err -> {},
                        ()->{
                            try {
                                emitter.send(SseEmitter.event().name("done").data("done"));
                            } catch (Exception ignored) {}
                            emitter.complete();
                        }
                );
        return emitter;
    }

    @Transactional
    public PlanResponse saveGeneratedPlan(Long userId, SaveGeneratedRequest request) {
        SaveGeneratedRequest.ParsedContent parsed = AiUtils.parseFromMarkdown(request.getContent());

        // 检查用户是否有正在生成中的规划
        boolean hasGeneratingPlan = planRepository
                .findByUserIdAndStatusAndIsDeletedFalse(userId, PlanStatus.generating, PageRequest.of(0, 1))
                .getTotalElements() > 0;

        // 如果有正在生成中的规划，新规划状态为draft；否则为generating（正在进行中）
        PlanStatus initialStatus = hasGeneratingPlan ? PlanStatus.draft : PlanStatus.generating;

        // 1. 保存规划
         Plan plan = Plan.builder()
                .userId(userId)
                .title(parsed.getTitle())
                .type(Plan.PlanType.valueOf(request.getType()))
                .destination(request.getDestination())
                .formData(request.getFormData())
                .status(initialStatus)
                .startDate(parsed.getStartDate())
                .endDate(parsed.getEndDate())
                .planDate(parsed.getPlanDate())
                .resource(new ArrayList<>())
                .build();
        plan = planRepository.save(plan);

        // 2. 保存阶段
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

        // 3. 保存任务
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
                            .status(TaskStatus.pending)
                            .build();
                    tasks.add(task);
                }
            }
        }
        taskRepository.saveAll(tasks);

        // 4. 并行生成资源推荐
        final Long savedPlanId = plan.getId();
        final String type = request.getType();
        final Map<String, Object> destination = request.getDestination();
        final Map<String, Object> formData = request.getFormData();
        CompletableFuture.runAsync(() -> {
            try {
                generateResourceRecommend(savedPlanId, type, destination, formData);
            } catch (Exception e) {
                log.error("生成资源推荐失败, planId: {}", savedPlanId, e);
            }
        });

        return planMapper.toResponse(plan);
    }

    /**
     * AI 生成资源推荐并更新到 plan
     */
    private void generateResourceRecommend(Long planId, String type, Map<String, Object> destination, Map<String, Object> formData) {
        String prompt = AiUtils.buildResourceRecommendPrompt(type, destination, formData);
        String systemPrompt = "你是一个专业的出国实用资源推荐顾问，精通各国签证、住宿、交通、餐饮、支付等资源的查找和推荐。请用简洁的中文回复，直接返回 JSON 数组，不要有其他解释性文字。";

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();

        List<Map<String, Object>> resources = AiUtils.parseResourceRecommend(response);

        planRepository.findById(planId).ifPresent(plan -> {
            plan.setResource(resources);
            planRepository.save(plan);
        });
    }

    @Transactional
    public void reorderPhases(Long userId, Long planId, ReorderRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该规划");
        }

        List<Long> phaseIds = request.getPhaseIds();
        IntStream.range(0, phaseIds.size()).forEach(i -> {
            Long phaseId = phaseIds.get(i);
            phaseRepository.findById(phaseId).ifPresent(phase -> {
                phase.setSortOrder(i);
                phaseRepository.save(phase);
            });
        });
    }

    public PlanResponse getGeneratingPlan(Long userId) {
        return planRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, PlanStatus.generating)
                .map(planMapper::toResponse)
                .orElse(null);
    }
}
