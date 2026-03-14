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
import reactor.core.publisher.Flux;

import java.util.*;
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

    @Transactional
    public PlanResponse generatePlan(Long userId, GeneratePlanRequest request) {
        // 构建AI提示词
        String prompt = buildPrompt(request);

        // 根据类型获取对应的AI专家
        String systemPrompt = getSystemPromptByType(request.getType());

        // 调用AI生成规划
        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();

        // 解析AI响应
        Map<String, Object> planData = parseAiResponse(aiResponse, request);

        // 保存规划到数据库
        Plan plan = Plan.builder()
                .userId(userId)
                .title((String) planData.getOrDefault("title", "新规划"))
                .type(Plan.PlanType.valueOf(request.getType()))
                .destination(request.getDestination())
                .formData(request.getFormData())
                .status("completed")
                .build();
        plan = planRepository.save(plan);

        // 保存阶段和任务
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> phasesData = (List<Map<String, Object>>) planData.getOrDefault("phases", new ArrayList<>());

        // 创建一个映射：phase title -> phase index
        Map<String, Integer> phaseIndexMap = new HashMap<>();

        // 先保存所有阶段
        List<PlanPhase> phases = new ArrayList<>();
        int phaseOrder = 0;
        for (Map<String, Object> phaseData : phasesData) {
            PlanPhase phase = PlanPhase.builder()
                    .planId(plan.getId())
                    .title((String) phaseData.getOrDefault("title", ""))
                    .description((String) phaseData.getOrDefault("description", ""))
                    .sortOrder(phaseOrder)
                    .build();
            phaseIndexMap.put((String) phaseData.getOrDefault("title", ""), phaseOrder);
            phases.add(phase);
            phaseOrder++;
        }
        phases = phaseRepository.saveAll(phases);

        // 再保存所有任务
        List<PlanTask> tasks = new ArrayList<>();
        for (int i = 0; i < phases.size(); i++) {
            PlanPhase phase = phases.get(i);
            Map<String, Object> phaseData = phasesData.get(i);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasksData = (List<Map<String, Object>>) phaseData.getOrDefault("tasks", new ArrayList<>());

            int taskOrder = 0;
            for (Map<String, Object> taskData : tasksData) {
                PlanTask task = PlanTask.builder()
                        .phaseId(phase.getId())
                        .title((String) taskData.getOrDefault("title", ""))
                        .description((String) taskData.getOrDefault("description", ""))
                        .aiSuggestion((String) taskData.getOrDefault("aiSuggestion", ""))
                        .sortOrder(taskOrder++)
                        .isCompleted(false)
                        .build();
                tasks.add(task);
            }
        }
        taskRepository.saveAll(tasks);

        return planMapper.toResponse(plan);
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

    private Map<String, Object> parseAiResponse(String aiResponse, GeneratePlanRequest request) {
        Map<String, Object> planData = new HashMap<>();

        // 尝试解析JSON
        try {
            // 尝试提取JSON部分（如果AI返回有markdown格式）
            String jsonStr = aiResponse.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = new com.fasterxml.jackson.databind.ObjectMapper().readValue(jsonStr, Map.class);

            planData.put("title", parsed.getOrDefault("title", "新规划"));
            planData.put("type", request.getType());
            planData.put("destination", request.getDestination());
            planData.put("status", "generating");
            planData.put("formData", request.getFormData());
            planData.put("phases", parsed.getOrDefault("phases", new ArrayList<>()));

        } catch (Exception e) {
            log.error("解析AI响应失败，使用默认规划", e);
            // 解析失败时返回默认规划
            planData.put("title", request.getFormData().getOrDefault("title", "新规划"));
            planData.put("type", request.getType());
            planData.put("destination", request.getDestination());
            planData.put("status", "generating");
            planData.put("formData", request.getFormData());
            planData.put("phases", new ArrayList<>());
        }

        return planData;
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

    /**
     * 流式生成规划 - 返回AI流式响应
     */
    public Flux<String> generatePlanStream(Long userId, GeneratePlanRequest request) {
        // 构建AI提示词
        String prompt = buildPrompt(request);

        // 根据类型获取对应的AI专家
        String systemPrompt = getSystemPromptByType(request.getType());

        // 流式调用AI，返回Flux<String>
        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .stream()
                .content();
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
