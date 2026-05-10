package com.goAbroad.core.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    private String status;
    private String priority;
    private String aiSuggestion;
    private Map<String, Object> formData;
    private LocalDateTime reminderTime;
    private List<Map<String, Object>> attachments;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate planDate;
    private Integer sortOrder;
}
