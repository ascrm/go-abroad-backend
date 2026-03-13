package com.goAbroad.core.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionResponse {
    private Boolean success;
    private String action;
    private Boolean isActive;
}
