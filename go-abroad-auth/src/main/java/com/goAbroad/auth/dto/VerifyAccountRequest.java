package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 验证账号请求（用于找回密码时验证账号是否属于当前用户）
 */
@Data
public class VerifyAccountRequest {

    @NotNull(message = "账号类型不能为空")
    private Integer accountType;

    @NotNull(message = "账号值不能为空")
    private String accountValue;
}