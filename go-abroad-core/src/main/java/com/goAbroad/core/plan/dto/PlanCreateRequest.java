package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PlanCreateRequest {
    private String title;
    private String type;
    private Map<String, Object> destination;
    private Map<String, Object> formData;
}
