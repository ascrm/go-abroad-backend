package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 第三方登录请求
 * 简化版：前端只需传递 socialType 和 code，后端自动换取 token 并获取用户信息
 */
@Data
public class SocialLoginRequest {

    @NotNull(message = "平台类型不能为空")
    private Integer socialType;  // 平台类型: 1-微信, 2-QQ, 3-Google, 4-Apple, 5-抖音

    // 授权码（前端从第三方获取的一次性 code）
    @NotBlank(message = "授权码不能为空")
    private String code;
}
