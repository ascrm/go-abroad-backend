package com.goAbroad.core.community.repository;

import com.goAbroad.core.community.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdAndIsDeletedFalse(Long questionId);

    List<Answer> findByQuestionIdOrderByCreatedAtDesc(Long questionId);
}
