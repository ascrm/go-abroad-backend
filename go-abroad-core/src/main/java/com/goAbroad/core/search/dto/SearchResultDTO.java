package com.goAbroad.core.search.dto;

import lombok.*;

import java.util.List;

/**
 * 搜索结果汇总
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultDTO {
    private List<ArticleSearchDTO> articles;
    private List<PlanSearchDTO> plans;
    private List<QuestionSearchDTO> questions;
    private List<UserSearchDTO> users;
}