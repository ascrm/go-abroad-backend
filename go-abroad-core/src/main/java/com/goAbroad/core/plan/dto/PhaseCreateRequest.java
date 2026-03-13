package com.goAbroad.core.plan.dto;

import lombok.Data;

@Data
public class PhaseCreateRequest {
    private Long planId;
    private String title;
    private String description;
    private Integer sortOrder;
}
