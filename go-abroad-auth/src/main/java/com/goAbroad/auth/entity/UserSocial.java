package com.goAbroad.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 第三方登录表 - 存储第三方登录信息
 */
@Entity
@Table(name = "tb_user_social",
       uniqueConstraints = @UniqueConstraint(columnNames = {"social_type", "openid"}),
       indexes = @Index(columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;              // 关联用户

    @Column(name = "social_type", nullable = false)
    private Integer socialType;     // 平台类型: 1-微信, 2-QQ, 3-Google, 4-Apple, 5-抖音

    @Column(name = "openid", nullable = false, length = 100)
    private String openid;          // 第三方平台openid

    @Column(name = "unionid", length = 100)
    private String unionid;         // 微信/QQ unionid

    @Column(name = "access_token", length = 500)
    private String accessToken;    // 访问令牌

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;   // 刷新令牌

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // 令牌过期时间

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 第三方平台类型枚举
     */
    public static final class SocialType {
        public static final int WECHAT = 1;   // 微信
        public static final int QQ = 2;       // QQ
        public static final int GOOGLE = 3;   // Google
        public static final int APPLE = 4;   // Apple
        public static final int DOUYIN = 5;  // 抖音
    }
}
