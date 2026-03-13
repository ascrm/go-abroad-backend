package com.goAbroad.core.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCreateRequest {
    private String title;
    private String description;
    private String content;
    private String image;
    private String tag;
    private Boolean isPublished;
    private Boolean isFeatured;
}
