package com.goAbroad.core.plan.repository;

import com.goAbroad.core.plan.entity.PlanTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTaskRepository extends JpaRepository<PlanTask, Long> {

    List<PlanTask> findByPhaseIdOrderBySortOrderAsc(Long phaseId);

    void deleteByPhaseId(Long phaseId);

    default List<PlanTask> findByPhaseIdOrderBySortOrder(Long phaseId) {
        return findByPhaseIdOrderBySortOrderAsc(phaseId);
    }
}
