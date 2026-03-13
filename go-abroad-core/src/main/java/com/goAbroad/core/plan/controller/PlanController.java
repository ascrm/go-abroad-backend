package com.goAbroad.core.plan.controller;

import com.goAbroad.common.result.PageR;
import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import com.goAbroad.core.plan.dto.*;
import com.goAbroad.core.plan.service.PlanServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanServiceImpl planService;

    @GetMapping("/list")
    public R<PageR<PlanResponse>> getPlanList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserHolder.getUserId();
        PageR<PlanResponse> result = planService.getPlanList(userId, type, status, page, pageSize);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<PlanDetailResponse> getPlanDetail(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        PlanDetailResponse result = planService.getPlanDetail(userId, id);
        return R.ok(result);
    }

    @PostMapping
    public R<PlanResponse> createPlan(@RequestBody PlanCreateRequest planRequest) {
        Long userId = UserHolder.getUserId();
        PlanResponse result = planService.createPlan(userId, planRequest);
        return R.ok(result);
    }

    @PutMapping("/{id}")
    public R<PlanResponse> updatePlan(@PathVariable Long id, @RequestBody PlanUpdateRequest planRequest) {
        Long userId = UserHolder.getUserId();
        PlanResponse result = planService.updatePlan(userId, id, planRequest);
        return R.ok(result);
    }

    @DeleteMapping("/{id}")
    public R<Void> deletePlan(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        planService.deletePlan(userId, id);
        return R.ok();
    }

    @PostMapping("/generate")
    public R<Map<String, Object>> generatePlan(@RequestBody GeneratePlanRequest request) {
        Long userId = UserHolder.getUserId();
        Map<String, Object> result = planService.generatePlan(userId, request);
        return R.ok(result);
    }

    @PostMapping("/save-generated")
    public R<PlanResponse> saveGeneratedPlan(@RequestBody SaveGeneratedRequest request) {
        Long userId = UserHolder.getUserId();
        PlanResponse result = planService.saveGeneratedPlan(userId, request);
        return R.ok(result);
    }

    @PutMapping("/{planId}/phases/reorder")
    public R<Void> reorderPhases(@PathVariable Long planId, @RequestBody ReorderRequest request) {
        Long userId = UserHolder.getUserId();
        planService.reorderPhases(userId, planId, request);
        return R.ok();
    }
}
