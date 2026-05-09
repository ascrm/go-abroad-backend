package com.goAbroad.core.community.repository;

import com.goAbroad.core.community.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdAndIsDeletedFalse(Long questionId);

    List<Answer> findByQuestionIdOrderByCreatedAtDesc(Long questionId);

    /** 获取回答中点赞最高的 */
    List<Answer> findTop1ByQuestionIdAndIsDeletedFalseOrderByLikesDesc(Long questionId);
}
