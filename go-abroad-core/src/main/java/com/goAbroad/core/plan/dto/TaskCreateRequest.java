package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TaskCreateRequest {
    private Long phaseId;
    private String title;
    private String description;
    private String aiSuggestion;
    private Map<String, Object> quickEntries;
    private Integer sortOrder;
}
