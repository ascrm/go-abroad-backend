package com.goAbroad.core.plan.repository;

import com.goAbroad.core.plan.entity.Plan;
import com.goAbroad.core.plan.enums.PlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Page<Plan> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Page<Plan> findByUserIdAndTypeAndIsDeletedFalse(Long userId, Plan.PlanType type, Pageable pageable);

    Page<Plan> findByUserIdAndStatusAndIsDeletedFalse(Long userId, PlanStatus status, Pageable pageable);

    Page<Plan> findByUserIdAndTypeAndStatusAndIsDeletedFalse(Long userId, Plan.PlanType type, PlanStatus status, Pageable pageable);

    List<Plan> findByUserIdAndIsDeletedFalse(Long userId);

    Optional<Plan> findByUserIdAndStatusAndIsDeletedFalse(Long userId, PlanStatus status);

    default Page<Plan> findByUserIdWithFilters(Long userId, String type, String status, Pageable pageable) {
        Plan.PlanType planType = null;
        PlanStatus planStatus = null;
        if (type != null && !type.isEmpty()) {
            try {
                planType = Plan.PlanType.valueOf(type);
            } catch (IllegalArgumentException e) {
                // ignore invalid type
            }
        }
        if (status != null && !status.isEmpty()) {
            try {
                planStatus = PlanStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // ignore invalid status
            }
        }

        if (planType != null && planStatus != null) {
            return findByUserIdAndTypeAndStatusAndIsDeletedFalse(userId, planType, planStatus, pageable);
        } else if (planType != null) {
            return findByUserIdAndTypeAndIsDeletedFalse(userId, planType, pageable);
        } else if (planStatus != null) {
            return findByUserIdAndStatusAndIsDeletedFalse(userId, planStatus, pageable);
        } else {
            return findByUserIdAndIsDeletedFalse(userId, pageable);
        }
    }
}
