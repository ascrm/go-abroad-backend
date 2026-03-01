package com.goAbroad.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 账号表 - 登录凭证（支持用户名、邮箱、手机号登录）
 */
@Entity
@Table(name = "tb_user_account",
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_type", "account_value"}),
       indexes = @Index(columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;              // 关联用户

    @Column(name = "account_type", nullable = false)
    private Integer accountType;    // 账号类型: 1-用户名, 2-邮箱, 3-手机号

    @Column(name = "account_value", nullable = false, length = 100)
    private String accountValue;    // 账号值（用户名/邮箱/手机号）

    @Column(length = 255)
    private String password;        // 密码（第三方登录时为空）

    @Column(length = 50)
    private String salt;            // 盐值

    @Column(name = "verified")
    private Boolean verified = false; // 是否验证

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
     * 账号类型枚举
     */
    public static final class AccountType {
        public static final int USERNAME = 1;  // 用户名
        public static final int EMAIL = 2;    // 邮箱
        public static final int PHONE = 3;    // 手机号
    }
}
