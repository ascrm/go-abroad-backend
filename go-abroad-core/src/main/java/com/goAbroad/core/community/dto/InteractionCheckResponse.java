package com.goAbroad.core.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionCheckResponse {
    private Boolean isFavorited;
    private Boolean isLiked;
    private Boolean isFollowed;
}
