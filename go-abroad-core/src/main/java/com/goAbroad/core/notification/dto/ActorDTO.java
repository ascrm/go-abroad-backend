package com.goAbroad.core.notification.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorDTO {
    private Long userId;
    private String nickname;
    private String avatar;
}