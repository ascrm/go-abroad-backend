package com.goAbroad.core.plan.controller;

import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import com.goAbroad.core.plan.dto.*;
import com.goAbroad.core.plan.service.PhaseServiceImpl;
import com.goAbroad.core.plan.service.TaskServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskServiceImpl taskService;
    private final PhaseServiceImpl phaseService;

    @GetMapping("/{phaseId}/tasks")
    public R<List<TaskResponse>> getTasksByPhaseId(@PathVariable Long phaseId) {
        Long userId = UserHolder.getUserId();
        List<TaskResponse> result = taskService.getTasksByPhaseId(userId, phaseId);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<TaskResponse> getTaskById(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        TaskResponse result = taskService.getTaskById(userId, id);
        return R.ok(result);
    }

    @PostMapping()
    public R<TaskResponse> createTask(@RequestBody TaskCreateRequest request) {
        Long userId = UserHolder.getUserId();
        TaskResponse result = taskService.createTask(userId, request);
        return R.ok(result);
    }

    @PutMapping("/{id}")
    public R<TaskResponse> updateTask(@PathVariable Long id, @RequestBody TaskUpdateRequest request) {
        Long userId = UserHolder.getUserId();
        TaskResponse result = taskService.updateTask(userId, id, request);
        return R.ok(result);
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteTask(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        taskService.deleteTask(userId, id);
        return R.ok();
    }

    @PutMapping("/{id}/complete")
    public R<TaskResponse> completeTask(@PathVariable Long id, @RequestBody TaskCompleteRequest request) {
        Long userId = UserHolder.getUserId();
        TaskResponse result = taskService.completeTask(userId, id, request);
        return R.ok(result);
    }

    @PutMapping("/{phaseId}/tasks/reorder")
    public R<Void> reorderTasks(@PathVariable Long phaseId, @RequestBody ReorderRequest request) {
        Long userId = UserHolder.getUserId();
        if (request.getTaskIds() != null && !request.getTaskIds().isEmpty()) {
            phaseService.reorderTasks(userId, phaseId, request);
        }
        return R.ok();
    }

    @GetMapping("/{taskId}/ai-suggestion")
    public R<Map<String, String>> getAiSuggestion(@PathVariable Long taskId) {
        Long userId = UserHolder.getUserId();
        String suggestion = taskService.getAiSuggestion(userId, taskId);
        return R.ok(Map.of("suggestion", suggestion));
    }
}
