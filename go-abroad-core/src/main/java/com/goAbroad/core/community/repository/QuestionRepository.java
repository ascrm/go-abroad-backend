package com.goAbroad.core.community.repository;

import com.goAbroad.core.community.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.isDeleted = false " +
            "AND (:category IS NULL OR q.category = :category) " +
            "AND (:isResolved IS NULL OR q.isResolved = :isResolved) " +
            "ORDER BY q.createdAt DESC")
    Page<Question> findByCondition(@Param("category") String category,
                                   @Param("isResolved") Boolean isResolved,
                                   Pageable pageable);

    List<Question> findByIsDeletedFalse();
}
