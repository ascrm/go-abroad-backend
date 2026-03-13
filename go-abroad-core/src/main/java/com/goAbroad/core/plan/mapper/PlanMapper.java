package com.goAbroad.core.plan.mapper;

import com.goAbroad.core.plan.dto.*;
import com.goAbroad.core.plan.entity.Plan;
import com.goAbroad.core.plan.entity.PlanPhase;
import com.goAbroad.core.plan.entity.PlanTask;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PlanMapper {

    PlanMapper INSTANCE = Mappers.getMapper(PlanMapper.class);

    @Mapping(target = "type", expression = "java(plan.getType().name())")
    PlanResponse toResponse(Plan plan);

    List<PlanResponse> toResponseList(List<Plan> plans);

    @Mapping(target = "type", expression = "java(plan.getType().name())")
    PlanDetailResponse toDetailResponse(Plan plan);

    PhaseResponse toPhaseResponse(PlanPhase phase);

    List<PhaseResponse> toPhaseResponseList(List<PlanPhase> phases);

    TaskResponse toTaskResponse(PlanTask task);

    List<TaskResponse> toTaskResponseList(List<PlanTask> tasks);


    @Mapping(target = "type", expression = "java(Plan.PlanType.valueOf(request.getType()))")
    @Mapping(target = "status", constant = "draft")
    Plan toEntity(PlanCreateRequest request);

    void updateFromRequest(PlanUpdateRequest request, @MappingTarget Plan plan);

    PlanPhase toPhaseEntity(PhaseCreateRequest request);

    void updatePhaseFromRequest(PhaseUpdateRequest request, @MappingTarget PlanPhase phase);

    @Mapping(target = "isCompleted", constant = "false")
    PlanTask toTaskEntity(TaskCreateRequest request);

    void updateTaskFromRequest(TaskUpdateRequest request, @MappingTarget PlanTask task);
}
