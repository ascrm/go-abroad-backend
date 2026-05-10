package com.goAbroad.core.profile.repository;

import com.goAbroad.core.profile.entity.BrowseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {
    List<BrowseHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}