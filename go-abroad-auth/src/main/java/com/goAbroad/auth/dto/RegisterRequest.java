package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 邮箱/手机号注册请求
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "账号不能为空")
    private String account;     // 账号（邮箱或手机号）

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;    // 密码

    /**
     * 账号类型: 2-邮箱, 3-手机号
     * 如果不传，则根据 account 自动判断
     */
    private Integer accountType;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
