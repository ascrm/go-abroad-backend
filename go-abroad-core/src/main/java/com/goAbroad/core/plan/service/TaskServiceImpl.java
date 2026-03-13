package com.goAbroad.core.plan.service;

import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.core.plan.dto.TaskCompleteRequest;
import com.goAbroad.core.plan.dto.TaskCreateRequest;
import com.goAbroad.core.plan.dto.TaskResponse;
import com.goAbroad.core.plan.dto.TaskUpdateRequest;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl {

    private final PlanRepository planRepository;
    private final PlanPhaseRepository phaseRepository;
    private final PlanTaskRepository taskRepository;
    private final PlanMapper planMapper;

    public List<TaskResponse> getTasksByPhaseId(Long userId, Long phaseId) {
        PlanPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该任务");
        }

        List<PlanTask> tasks = taskRepository.findByPhaseIdOrderBySortOrder(phaseId);
        return planMapper.toTaskResponseList(tasks);
    }

    public TaskResponse getTaskById(Long userId, Long taskId) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        PlanPhase phase = phaseRepository.findById(task.getPhaseId())
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该任务");
        }

        return planMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse createTask(Long userId, TaskCreateRequest request) {
        PlanPhase phase = phaseRepository.findById(request.getPhaseId())
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该规划");
        }

        PlanTask task = planMapper.toTaskEntity(request);
        task.setPhaseId(request.getPhaseId());
        task.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        task = taskRepository.save(task);
        return planMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskUpdateRequest request) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        PlanPhase phase = phaseRepository.findById(task.getPhaseId())
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该任务");
        }

        if (request.getIsCompleted() != null) {
            task.setIsCompleted(request.getIsCompleted());
            if (request.getIsCompleted()) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(null);
            }
        }

        planMapper.updateTaskFromRequest(request, task);
        task = taskRepository.save(task);
        return planMapper.toTaskResponse(task);
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        PlanPhase phase = phaseRepository.findById(task.getPhaseId())
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该任务");
        }

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse completeTask(Long userId, Long taskId, TaskCompleteRequest request) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        PlanPhase phase = phaseRepository.findById(task.getPhaseId())
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该任务");
        }

        task.setIsCompleted(request.getIsCompleted());
        if (request.getIsCompleted()) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        task = taskRepository.save(task);
        return planMapper.toTaskResponse(task);
    }

    public String getAiSuggestion(Long userId, Long taskId) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        PlanPhase phase = phaseRepository.findById(task.getPhaseId())
                .orElseThrow(() -> new BusinessException("阶段不存在"));

        Plan plan = planRepository.findById(phase.getPlanId())
                .orElseThrow(() -> new BusinessException("规划不存在"));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该任务");
        }

        if (task.getAiSuggestion() != null && !task.getAiSuggestion().isEmpty()) {
            return task.getAiSuggestion();
        }

        return "建议：" + task.getTitle() + " - " + (task.getDescription() != null ? task.getDescription() : "请按计划完成");
    }
}
