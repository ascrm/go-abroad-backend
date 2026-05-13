package com.goAbroad.core.notification.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private String content;
    private Boolean isRead;
    private Boolean isPinned;
    private Long relatedId;
    private String relatedType;
    private ActorDTO actor;
    private String time;
    private LocalDateTime createdAt;
}