package com.goAbroad.core.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCategoryResponse {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Boolean isActive;
}
