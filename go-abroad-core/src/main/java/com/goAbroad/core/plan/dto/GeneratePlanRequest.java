package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GeneratePlanRequest {
    private String type;
    private Map<String, Object> destination;
    private Map<String, Object> formData;
}
