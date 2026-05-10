package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PhaseUpdateRequest {
    private String title;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate planDate;
    private LocalDateTime reminderTime;
    private Boolean isMilestone;
    private Integer sortOrder;
}
