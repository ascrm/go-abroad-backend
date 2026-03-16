package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SaveGeneratedRequest {
    private String type;
    private Map<String, Object> destination;
    private Map<String, Object> formData;
    private String content;
    private String parseKey; // Redis key，用于从缓存获取解析好的 JSON

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
    }

    /**
     * AI 解析 content 后的结构
     */
    @Data
    public static class ParsedContent {
        private String title;
        private List<PhaseDto> phases;
    }
}
