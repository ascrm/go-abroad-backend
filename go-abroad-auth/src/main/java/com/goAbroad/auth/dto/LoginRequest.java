package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 账号密码登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "账号不能为空")
    private String account;     // 账号（用户名/邮箱/手机号）

    @NotBlank(message = "密码不能为空")
    private String password;    // 密码
}
