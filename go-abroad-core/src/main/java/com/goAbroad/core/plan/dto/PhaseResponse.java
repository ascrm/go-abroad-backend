package com.goAbroad.core.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private Integer sortOrder;
    private List<TaskResponse> tasks;
    private LocalDateTime createdAt;
}
