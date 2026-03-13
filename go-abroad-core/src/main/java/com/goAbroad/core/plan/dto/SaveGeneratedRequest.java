package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SaveGeneratedRequest {
    private String title;
    private String type;
    private Map<String, Object> destination;
    private Map<String, Object> formData;
    private List<PhaseDto> phases;

    @Data
    public static class PhaseDto {
        private String title;
        private String description;
        private List<TaskDto> tasks;
    }

    @Data
    public static class TaskDto {
        private String title;
        private String description;
        private String aiSuggestion;
    }
}
