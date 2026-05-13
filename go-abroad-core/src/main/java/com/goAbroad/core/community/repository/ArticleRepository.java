package com.goAbroad.core.community.repository;

import com.goAbroad.core.community.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a WHERE a.isPublished = true " +
            "AND (:tag IS NULL OR a.tag = :tag) " +
            "AND (:isFeatured IS NULL OR a.isFeatured = :isFeatured) " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findByCondition(@Param("tag") String tag,
                                  @Param("isFeatured") Boolean isFeatured,
                                  Pageable pageable);

    List<Article> findByIsPublishedTrue();

    List<Article> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @Query(value = "SELECT * FROM tb_articles WHERE is_published = true AND (title &@ :keyword OR description &@ :keyword) LIMIT :limit", nativeQuery = true)
    List<Article> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);
}
