package com.goAbroad.core.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String image;
    private String tag;
    private Long authorId;
    private AuthorDTO author;
    private Integer views;
    private Integer favorites;
    private Boolean isPublished;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isFavorited;
    private Boolean isLiked;
}
