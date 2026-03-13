package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PlanUpdateRequest {
    private String title;
    private Map<String, Object> destination;
    private Map<String, Object> formData;
    private String status;
}
