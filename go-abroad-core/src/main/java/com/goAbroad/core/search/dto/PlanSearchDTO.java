package com.goAbroad.core.search.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 搜索结果DTO - 规划
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanSearchDTO {
    private Long id;
    private String title;
    private String description;
    private String tag = "规划";
}