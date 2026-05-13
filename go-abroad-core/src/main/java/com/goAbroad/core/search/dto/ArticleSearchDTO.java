package com.goAbroad.core.search.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 搜索结果DTO - 文章
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleSearchDTO {
    private Long id;
    private String title;
    private String description;
    private String tag;
    private String time;
}