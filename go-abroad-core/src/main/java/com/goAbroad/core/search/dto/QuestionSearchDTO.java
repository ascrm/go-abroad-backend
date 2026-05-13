package com.goAbroad.core.search.dto;

import lombok.*;

/**
 * 搜索结果DTO - 问答
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSearchDTO {
    private Long id;
    private String title;
    private String category;
    private String tag = "问答";
}