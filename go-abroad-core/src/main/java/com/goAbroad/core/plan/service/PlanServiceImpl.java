package com.goAbroad.core.plan.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
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
     */
    public SseEmitter generatePlanStream(Long userId, GeneratePlanRequest request) {
        // 构建AI提示词
        String prompt = buildPrompt(request);

        // 根据类型获取对应的AI专家
        String systemPrompt = getSystemPromptByType(request.getType());

        // 创建SseEmitter，超时时间5分钟
        SseEmitter emitter = new SseEmitter(300_000L);

        // 【关键】增加状态标志位，防止连接断开后继续写数据
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        // 统一的清理方法
        Runnable cleanup = () -> {
            if (isCompleted.compareAndSet(false, true)) {
                emitter.complete();
            }
        };

        // 注册回调
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> {
            log.warn("SSE 错误: {}", e.getMessage());
            cleanup.run();
        });

        try {
            emitter.send(SseEmitter.event().comment("init"));
        } catch (IOException e) {
            log.warn("SSE 握手失败: {}", e.getMessage());
            cleanup.run();
            return emitter;
        }

        // 使用SseEmitter处理异步流式响应
        chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .stream()
                .content()
                .subscribe(
                        chunk -> {
                            if (isCompleted.get()) return; // 如果已断开，直接跳过
                            try {
                                emitter.send(SseEmitter.event().data(chunk));
                            } catch (Exception e) {
                                log.warn("前端断开连接: {}", e.getMessage());
                                cleanup.run(); // 报错立即清理
                            }
                        },
                        error -> {
                            log.error("AI 响应错误: {}", error.getMessage());
                            cleanup.run();
                        },
                        () -> {
                            if (!isCompleted.get()) {
                                try {
                                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                                } catch (Exception ignored) {
                                }
                                cleanup.run();
                            }
                        }
                );

        return emitter;
    }

    private String buildPrompt(GeneratePlanRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下信息生成一个简洁的留学/出境规划。\n");
        sb.append("请以JSON格式返回，包含以下字段：\n");
        sb.append("1. title: 规划标题\n");
        sb.append("2. phases: 阶段数组，每个阶段包含title、description、tasks\n");
        sb.append("3. 每个任务包含title、description\n");
        sb.append("4. 只生成3-4个阶段，每个阶段3-4个任务\n");
        sb.append("5. 用简洁的中文\n\n");

        sb.append("用户信息：\n");
        if (request.getDestination() != null) {
            sb.append("目的地：").append(request.getDestination()).append("\n");
        }
        if (request.getFormData() != null) {
            for (Map.Entry<String, Object> entry : request.getFormData().entrySet()) {
                sb.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
        }

        sb.append("\n只返回JSON。");

        return sb.toString();
    }

    private String getSystemPromptByType(String type) {
        if (type == null) {
            return "你是一个专业的出境规划顾问，请用简洁的中文回复。";
        }
        return switch (type.toLowerCase()) {
            case "study" -> "你是一个专业的留学规划顾问，专长于申请流程、学校选择、签证办理等。请用简洁的中文回复。";
            case "tourism" -> "你是一个专业的出境旅游规划顾问，专长于行程安排、签证办理、景点推荐等。请用简洁的中文回复。";
            case "work" -> "你是一个专业的海外工作规划顾问，专长于职业规划、求职技巧、工签办理等。请用简洁的中文回复。";
            case "immigration" -> "你是一个专业的移民规划顾问，专长于各国移民政策、投资移民、技术移民等。请用简洁的中文回复。";
            default -> "你是一个专业的出境规划顾问，请用简洁的中文回复。";
        };
    }

    @Transactional
    public PlanResponse saveGeneratedPlan(Long userId, SaveGeneratedRequest request) {
        // 保存规划
        Plan plan = Plan.builder()
                .userId(userId)
                .title(request.getTitle())
                .type(Plan.PlanType.valueOf(request.getType()))
                .destination(request.getDestination())
                .formData(request.getFormData())
                .status("completed")
                .build();
        plan = planRepository.save(plan);

        // 批量保存阶段
        List<PlanPhase> phases = new ArrayList<>();
        if (request.getPhases() != null) {
            int phaseOrder = 0;
            for (SaveGeneratedRequest.PhaseDto phaseDto : request.getPhases()) {
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

        // 批量保存任务
        List<PlanTask> tasks = new ArrayList<>();
        for (int i = 0; i < phases.size(); i++) {
            PlanPhase phase = phases.get(i);
            SaveGeneratedRequest.PhaseDto phaseDto = request.getPhases().get(i);
            if (phaseDto.getTasks() != null) {
                int taskOrder = 0;
                for (SaveGeneratedRequest.TaskDto taskDto : phaseDto.getTasks()) {
                    PlanTask task = PlanTask.builder()
                            .phaseId(phase.getId())
                            .title(taskDto.getTitle())
                            .description(taskDto.getDescription())
                            .aiSuggestion(taskDto.getAiSuggestion())
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
