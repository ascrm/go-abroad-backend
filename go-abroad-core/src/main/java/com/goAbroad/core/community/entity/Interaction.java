package com.goAbroad.core.community.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 互动表（收藏、点赞、关注）
 */
@Entity
@Table(name = "tb_interactions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_target_type_action", columnNames = {"user_id", "target_id", "target_type", "action"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 互动目标类型
     */
    public enum TargetType {
        article, question, answer
    }

    /**
     * 互动动作
     */
    public enum Action {
        favorite, like, follow, view
    }
}
