package com.goAbroad.core.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private Long phaseId;
    private String title;
    private String description;
    private Boolean isCompleted;
    private String aiSuggestion;
    private Map<String, Object> quickEntries;
    private Integer sortOrder;
}
