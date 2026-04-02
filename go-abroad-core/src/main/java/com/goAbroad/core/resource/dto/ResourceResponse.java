package com.goAbroad.core.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {
    private Long id;
    private String country;
    private Long categoryId;
    private String title;
    private String description;
    private String url;
    private String webUrl;
    private String imageUrl;
    private String logo;
    private Boolean isFeatured;
    private Map<String, Object> meta;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String categoryName;
}
