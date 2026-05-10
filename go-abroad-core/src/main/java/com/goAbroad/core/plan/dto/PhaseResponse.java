package com.goAbroad.core.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseResponse {
    private Long id;
    private Long planId;
    private String title;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate planDate;
    private LocalDateTime reminderTime;
    private Boolean isMilestone;
    private Integer sortOrder;
    private List<TaskResponse> tasks;
    private LocalDateTime createdAt;
}
