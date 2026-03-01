package com.goAbroad.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表 - 存储用户基本信息
 */
@Entity
@Table(name = "tb_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true)
    private String username;        // 用户名

    @Column(length = 100)
    private String nickname;        // 昵称

    @Column(length = 500)
    private String avatar;         // 头像URL

    @Column(nullable = false)
    private Integer gender = 0;    // 性别: 0-未知, 1-男, 2-女

    private LocalDate birthday;    // 生日

    @Column(length = 500)
    private String bio;            // 个人简介

    @Column(nullable = false)
    private Integer status = 1;    // 状态: 0-禁用, 1-正常

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
}
