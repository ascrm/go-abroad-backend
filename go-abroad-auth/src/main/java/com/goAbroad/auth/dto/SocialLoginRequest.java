package com.goAbroad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 第三方登录请求
 */
@Data
public class SocialLoginRequest {

    @NotNull(message = "平台类型不能为空")
    private Integer socialType;  // 平台类型: 1-微信, 2-QQ, 3-Google, 4-Apple, 5-抖音

    @NotBlank(message = "openid不能为空")
    private String openid;        // 第三方平台openid

    private String unionid;       // 微信/QQ unionid

    private String accessToken;   // 第三方平台access_token

    private String refreshToken;  // 第三方平台refresh_token

    private Long expiresIn;      // 令牌过期时间（秒）

    // 用户信息（可选，首次登录时可能需要）
    private String nickname;
    private String avatar;
    private Integer gender;
}
