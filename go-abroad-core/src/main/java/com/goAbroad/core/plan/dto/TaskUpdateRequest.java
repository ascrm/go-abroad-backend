package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TaskUpdateRequest {
    private String title;
    private String description;
    private Boolean isCompleted;
    private String aiSuggestion;
    private Map<String, Object> quickEntries;
    private Integer sortOrder;
}
