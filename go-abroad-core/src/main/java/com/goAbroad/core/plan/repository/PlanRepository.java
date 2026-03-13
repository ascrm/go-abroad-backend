package com.goAbroad.core.plan.repository;

import com.goAbroad.core.plan.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Page<Plan> findByUserId(Long userId, Pageable pageable);

    Page<Plan> findByUserIdAndType(Long userId, Plan.PlanType type, Pageable pageable);

    Page<Plan> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Page<Plan> findByUserIdAndTypeAndStatus(Long userId, Plan.PlanType type, String status, Pageable pageable);

    List<Plan> findByUserId(Long userId);

    default Page<Plan> findByUserIdWithFilters(Long userId, String type, String status, Pageable pageable) {
        Plan.PlanType planType = null;
        if (type != null && !type.isEmpty()) {
            try {
                planType = Plan.PlanType.valueOf(type);
            } catch (IllegalArgumentException e) {
                // ignore invalid type
            }
        }

        if (planType != null && status != null && !status.isEmpty()) {
            return findByUserIdAndTypeAndStatus(userId, planType, status, pageable);
        } else if (planType != null) {
            return findByUserIdAndType(userId, planType, pageable);
        } else if (status != null && !status.isEmpty()) {
            return findByUserIdAndStatus(userId, status, pageable);
        } else {
            return findByUserId(userId, pageable);
        }
    }
}
