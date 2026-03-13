package com.goAbroad.core.plan.repository;

import com.goAbroad.core.plan.entity.PlanPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanPhaseRepository extends JpaRepository<PlanPhase, Long> {

    List<PlanPhase> findByPlanIdOrderBySortOrderAsc(Long planId);

    void deleteByPlanId(Long planId);

    default List<PlanPhase> findByPlanIdOrderBySortOrder(Long planId) {
        return findByPlanIdOrderBySortOrderAsc(planId);
    }
}
