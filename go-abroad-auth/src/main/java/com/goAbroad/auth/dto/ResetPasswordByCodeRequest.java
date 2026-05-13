package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 通过验证码重置密码请求
 */
@Data
public class ResetPasswordByCodeRequest {

    @NotNull(message = "账号类型不能为空")
    private Integer accountType;

    @NotBlank(message = "账号值不能为空")
    private String accountValue;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度为6-20位")
    private String newPassword;
}