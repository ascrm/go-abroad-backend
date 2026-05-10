package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TaskUpdateRequest {
    private String title;
    private String description;
    private String aiSuggestion;
    private Map<String, Object> formData;
    private String status;
    private String priority;
    private LocalDateTime reminderTime;
    private List<Map<String, Object>> attachments;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate planDate;
    private Integer sortOrder;
}
