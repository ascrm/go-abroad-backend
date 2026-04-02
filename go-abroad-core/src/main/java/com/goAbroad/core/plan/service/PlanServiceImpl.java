package com.goAbroad.core.plan.service;

import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.common.result.PageR;
import com.goAbroad.core.plan.dto.*;
import com.goAbroad.core.plan.entity.Plan;
import com.goAbroad.core.plan.entity.PlanPhase;
import com.goAbroad.core.plan.entity.PlanTask;
import com.goAbroad.core.plan.enums.PlanStatus;
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

        PlanStatus newStatus = PlanStatus.valueOf(request.getStatus());
        if (newStatus == PlanStatus.generating) {
            boolean hasGenerating = planRepository
                    .findByUserIdAndStatus(userId, PlanStatus.generating)
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

        // 2. 保存规划
        Plan plan = Plan.builder()
                .userId(userId)
                .title(parsed.getTitle())
                .type(Plan.PlanType.valueOf(request.getType()))
                .destination(request.getDestination())
                .formData(request.getFormData())
                .status(PlanStatus.completed)
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

    public PlanResponse getGeneratingPlan(Long userId) {
        return planRepository.findByUserIdAndStatus(userId, PlanStatus.generating)
                .map(planMapper::toResponse)
                .orElse(null);
    }
}
