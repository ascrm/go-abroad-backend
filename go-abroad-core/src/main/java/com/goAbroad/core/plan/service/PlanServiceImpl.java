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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl {

    private final PlanRepository planRepository;
    private final PlanPhaseRepository phaseRepository;
    private final PlanTaskRepository taskRepository;
    private final PlanMapper planMapper;

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

    public Map<String, Object> generatePlan(Long userId, GeneratePlanRequest request) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> planData = new HashMap<>();
        planData.put("title", request.getFormData().getOrDefault("title", "新规划"));
        planData.put("type", request.getType());
        planData.put("destination", request.getDestination());
        planData.put("status", "generating");
        planData.put("formData", request.getFormData());

        List<Map<String, Object>> phases = new ArrayList<>();
        Map<String, Object> phase1 = new HashMap<>();
        phase1.put("title", "前期准备");
        phase1.put("description", "完成出行前的准备工作");
        List<Map<String, Object>> tasks1 = new ArrayList<>();
        Map<String, Object> task1 = new HashMap<>();
        task1.put("title", "确定目的地");
        task1.put("description", "确定最终目的地");
        tasks1.add(task1);
        phase1.put("tasks", tasks1);
        phases.add(phase1);

        planData.put("phases", phases);
        result.put("plan", planData);

        return result;
    }

    @Transactional
    public PlanResponse saveGeneratedPlan(Long userId, SaveGeneratedRequest request) {
        Plan plan = Plan.builder()
                .userId(userId)
                .title(request.getTitle())
                .type(Plan.PlanType.valueOf(request.getType()))
                .destination(request.getDestination())
                .formData(request.getFormData())
                .status("completed")
                .build();

        plan = planRepository.save(plan);

        if (request.getPhases() != null) {
            int phaseOrder = 0;
            for (SaveGeneratedRequest.PhaseDto phaseDto : request.getPhases()) {
                PlanPhase phase = PlanPhase.builder()
                        .planId(plan.getId())
                        .title(phaseDto.getTitle())
                        .description(phaseDto.getDescription())
                        .sortOrder(phaseOrder++)
                        .build();
                phase = phaseRepository.save(phase);

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
                        taskRepository.save(task);
                    }
                }
            }
        }

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
