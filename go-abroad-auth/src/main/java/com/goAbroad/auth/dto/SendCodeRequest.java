package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送验证码请求
 */
@Data
public class SendCodeRequest {

    /**
     * 账号类型: 2-邮箱, 3-手机号
     */
    @NotNull(message = "账号类型不能为空")
    private Integer accountType;

    /**
     * 邮箱或手机号
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 验证码类型: 1-注册, 2-登录, 3-找回密码
     */
    @NotNull(message = "验证码类型不能为空")
    private Integer codeType;
}
