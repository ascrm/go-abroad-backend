package com.goAbroad.core.plan.dto;

import lombok.Data;

@Data
public class PhaseUpdateRequest {
    private String title;
    private String description;
    private Integer sortOrder;
}
