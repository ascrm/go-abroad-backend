package com.goAbroad.auth.dto;

import lombok.Data;

@Data
public class SwitchAccountRequest {
    private Integer accountType;
    private String accountValue;
    private Long userId;  // 目标用户ID（第三方登录时需要）
}