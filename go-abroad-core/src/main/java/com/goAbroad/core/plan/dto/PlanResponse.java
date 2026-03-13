package com.goAbroad.core.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private Long id;
    private Long userId;
    private String title;
    private String type;
    private Map<String, Object> destination;
    private String status;
    private Map<String, Object> formData;
    private String coverImage;
    private List<PhaseResponse> phases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
