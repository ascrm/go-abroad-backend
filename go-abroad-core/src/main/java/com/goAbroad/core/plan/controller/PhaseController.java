package com.goAbroad.core.plan.controller;

import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import com.goAbroad.core.plan.dto.PhaseCreateRequest;
import com.goAbroad.core.plan.dto.PhaseResponse;
import com.goAbroad.core.plan.dto.PhaseUpdateRequest;
import com.goAbroad.core.plan.dto.ReorderRequest;
import com.goAbroad.core.plan.service.PhaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phase")
@RequiredArgsConstructor
public class PhaseController {

    private final PhaseServiceImpl phaseService;

    /**** 获取规划下的所有阶段 ****/
    @GetMapping("/{planId}/phases")
    public R<List<PhaseResponse>> getPhasesByPlanId(@PathVariable Long planId) {
        Long userId = UserHolder.getUserId();
        List<PhaseResponse> result = phaseService.getPhasesByPlanId(userId, planId);
        return R.ok(result);
    }

    /**** 创建阶段 ****/
    @PostMapping()
    public R<PhaseResponse> createPhase(@RequestBody PhaseCreateRequest request) {
        Long userId = UserHolder.getUserId();
        PhaseResponse result = phaseService.createPhase(userId, request);
        return R.ok(result);
    }

    /**** 更新阶段 ****/
    @PutMapping("/{id}")
    public R<PhaseResponse> updatePhase(@PathVariable Long id, @RequestBody PhaseUpdateRequest request) {
        Long userId = UserHolder.getUserId();
        PhaseResponse result = phaseService.updatePhase(userId, id, request);
        return R.ok(result);
    }

    /**** 删除阶段 ****/
    @DeleteMapping("/{id}")
    public R<Void> deletePhase(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        phaseService.deletePhase(userId, id);
        return R.ok();
    }

    /**** 重新排序阶段内的任务 ****/
    @PutMapping("/{planId}/phases/reorder")
    public R<Void> reorderPhases(@PathVariable Long planId, @RequestBody ReorderRequest request) {
        Long userId = UserHolder.getUserId();
        phaseService.reorderTasks(userId, planId, request);
        return R.ok();
    }
}
