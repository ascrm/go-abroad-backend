package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 第三方注册请求
 * 支持 Google 和 Apple
 */
@Data
public class SocialRegisterRequest {

    @NotNull(message = "平台类型不能为空")
    private Integer socialType;  // 平台类型: 3-Google, 4-Apple

    @NotBlank(message = "openid不能为空")
    private String openid;       // 第三方平台openid

    private String unionid;      // Apple 的 unionid

    private String accessToken;  // 第三方平台access_token

    private String refreshToken; // 第三方平台refresh_token

    private Long expiresIn;      // 令牌过期时间（秒）

    // 用户信息（可选）
    private String nickname;
    private String avatar;
    private Integer gender;
}
