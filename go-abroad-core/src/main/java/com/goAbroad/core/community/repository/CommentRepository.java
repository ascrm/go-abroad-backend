package com.goAbroad.core.community.repository;

import com.goAbroad.core.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 获取回答的顶层评论（分页）
     */
    Page<Comment> findByAnswerIdAndParentIdIsNullAndIsDeletedFalseOrderByCreatedAtDesc(
            Long answerId, Pageable pageable);

    /**
     * 获取父评论的所有子评论
     */
    List<Comment> findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentId);

    /**
     * 统计回答的评论总数
     */
    long countByAnswerIdAndIsDeletedFalse(Long answerId);

    /**
     * 检查评论是否属于指定用户
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Comment c WHERE c.id = :commentId AND c.userId = :userId AND c.isDeleted = false")
    boolean existsByIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
}