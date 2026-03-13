package com.goAbroad.core.plan.service;

import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.core.plan.dto.PhaseCreateRequest;
import com.goAbroad.core.plan.dto.PhaseResponse;
import com.goAbroad.core.plan.dto.PhaseUpdateRequest;
import com.goAbroad.core.plan.dto.ReorderRequest;
import com.goAbroad.core.plan.dto.TaskResponse;
import com.goAbroad.core.plan.entity.Plan;
import com.goAbroad.core.plan.entity.PlanPhase;
import com.goAbroad.core.plan.entity.PlanTask;
import com.goAbroad.core.plan.mapper.PlanMapper;
import com.goAbroad.core.plan.repository.PlanPhaseRepository;
import com.goAbroad.core.plan.repository.PlanRepository;
import com.goAbroad.core.plan.repository.PlanTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhaseServiceImpl {

    private final PlanRepository planRepository;
    private final PlanPhaseRepository phaseRepository;
    private final PlanTaskRepository taskRepository;
    private final PlanMapper planMapper;

    public List<PhaseResponse> getPhasesByPlanId(Long userId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该规划");
        }

        List<PlanPhase> phases = phaseRepository.findByPlanIdOrderBySortOrder(planId);
        return phases.stream()
                .map(this::toPhaseResponseWithTasks)
                .collect(Collectors.toList());
    }

    public PhaseResponse getPhaseById(Long userId, Long phaseId) {
        PlanPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该阶段");
        }

        return toPhaseResponseWithTasks(phase);
    }

    @Transactional
    public PhaseResponse createPhase(Long userId, PhaseCreateRequest request) {
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该规划");
        }

        PlanPhase phase = planMapper.toPhaseEntity(request);
        phase.setPlanId(request.getPlanId());
        phase = phaseRepository.save(phase);
        return planMapper.toPhaseResponse(phase);
    }

    @Transactional
    public PhaseResponse updatePhase(Long userId, Long phaseId, PhaseUpdateRequest request) {
        PlanPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该阶段");
        }

        planMapper.updatePhaseFromRequest(request, phase);
        phase = phaseRepository.save(phase);
        return planMapper.toPhaseResponse(phase);
    }

    @Transactional
    public void deletePhase(Long userId, Long phaseId) {
        PlanPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该阶段");
        }

        taskRepository.deleteByPhaseId(phaseId);
        phaseRepository.delete(phase);
    }

    @Transactional
    public void reorderTasks(Long userId, Long phaseId, ReorderRequest request) {
        PlanPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该阶段");
        }

        List<Long> taskIds = request.getTaskIds();
        for (int i = 0; i < taskIds.size(); i++) {
            final int finalI = i;
            taskRepository.findById(taskIds.get(i)).ifPresent(task -> {
                task.setSortOrder(finalI);
                taskRepository.save(task);
            });
        }
    }

    private PhaseResponse toPhaseResponseWithTasks(PlanPhase phase) {
        List<PlanTask> tasks = taskRepository.findByPhaseIdOrderBySortOrder(phase.getId());
        List<TaskResponse> taskResponses = planMapper.toTaskResponseList(tasks);

        PhaseResponse response = planMapper.toPhaseResponse(phase);
        response.setTasks(taskResponses);
        return response;
    }
}
