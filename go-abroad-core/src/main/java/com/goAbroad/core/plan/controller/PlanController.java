package com.goAbroad.core.plan.controller;

import com.goAbroad.common.result.PageR;
import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import com.goAbroad.core.plan.dto.*;
import com.goAbroad.core.plan.service.PlanServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanServiceImpl planService;

    /**** 获取规划列表 ****/
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

    /**** 获取规划详情 ****/
    @GetMapping("/{id}")
    public R<PlanDetailResponse> getPlanDetail(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        PlanDetailResponse result = planService.getPlanDetail(userId, id);
        return R.ok(result);
    }

    /**** 创建规划 ****/
    @PostMapping
    public R<PlanResponse> createPlan(@RequestBody PlanCreateRequest planRequest) {
        Long userId = UserHolder.getUserId();
        PlanResponse result = planService.createPlan(userId, planRequest);
        return R.ok(result);
    }

    /**** 更新规划 ****/
    @PutMapping("/{id}")
    public R<PlanResponse> updatePlan(@PathVariable Long id, @RequestBody PlanUpdateRequest planRequest) {
        Long userId = UserHolder.getUserId();
        PlanResponse result = planService.updatePlan(userId, id, planRequest);
        return R.ok(result);
    }

    /**** 删除规划 ****/
    @DeleteMapping("/{id}")
    public R<Void> deletePlan(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        planService.deletePlan(userId, id);
        return R.ok();
    }

    /**** AI生成规划（流式） ****/
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generatePlanStream(@RequestBody GeneratePlanRequest request) {
        Long userId = UserHolder.getUserId();
        return planService.generatePlanStream(userId, request);
    }

    /**** AI生成规划 ****/
    @PostMapping("/generate")
    public R<PlanResponse> generatePlan(@RequestBody GeneratePlanRequest request) {
        Long userId = UserHolder.getUserId();
        PlanResponse result = planService.generatePlan(userId, request);
        return R.ok(result);
    }

    /**** 保存AI生成的规划 ****/
    @PostMapping("/save-generated")
    public R<PlanResponse> saveGeneratedPlan(@RequestBody SaveGeneratedRequest request) {
        Long userId = UserHolder.getUserId();
        PlanResponse result = planService.saveGeneratedPlan(userId, request);
        return R.ok(result);
    }

    /**** 重新排序阶段 ****/
    @PutMapping("/{planId}/phases/reorder")
    public R<Void> reorderPhases(@PathVariable Long planId, @RequestBody ReorderRequest request) {
        Long userId = UserHolder.getUserId();
        planService.reorderPhases(userId, planId, request);
        return R.ok();
    }
}
