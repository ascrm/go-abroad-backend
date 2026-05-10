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
public class PlanDetailResponse {
    private Long id;
    private Long userId;
    private String title;
    private String type;
    private Object destination;
    private String status;
    private Object formData;
    private String coverImage;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate planDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PhaseResponse> phases;
}
