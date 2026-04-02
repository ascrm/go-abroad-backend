package com.goAbroad.core.resource.repository;

import com.goAbroad.core.resource.entity.ResourceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceCategoryRepository extends JpaRepository<ResourceCategory, Long> {

    List<ResourceCategory> findByIsActiveTrueOrderBySortOrderAsc();
}
