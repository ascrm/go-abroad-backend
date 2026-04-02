package com.goAbroad.core.resource.repository;

import com.goAbroad.core.resource.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @Query("SELECT r FROM Resource r " +
            "LEFT JOIN FETCH r.category " +
            "WHERE r.isActive = true " +
            "AND (:country IS NULL OR r.country = :country) " +
            "AND (:categoryId IS NULL OR r.categoryId = :categoryId) " +
            "ORDER BY r.sortOrder ASC")
    List<Resource> findByCondition(
            @Param("country") String country,
            @Param("categoryId") Long categoryId);

    @Query("SELECT r FROM Resource r " +
            "LEFT JOIN FETCH r.category " +
            "WHERE r.isActive = true " +
            "AND r.isFeatured = true " +
            "ORDER BY r.sortOrder ASC")
    List<Resource> findFeaturedResources();

    List<Resource> findByCategoryIdAndIsActiveTrueOrderBySortOrderAsc(Long categoryId);
}
