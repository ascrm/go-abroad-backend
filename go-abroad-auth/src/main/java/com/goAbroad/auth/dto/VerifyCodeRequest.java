package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 验证验证码请求（用于找回密码时预验证）
 */
@Data
public class VerifyCodeRequest {

    @NotNull(message = "账号类型不能为空")
    private Integer accountType;

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "验证码不能为空")
    private String code;
}