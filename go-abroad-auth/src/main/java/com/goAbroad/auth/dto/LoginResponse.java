package com.goAbroad.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;     // 访问令牌
    private String refreshToken;     // 刷新令牌
    private Long expiresIn;          // 过期时间（秒）
    private UserInfo user;          // 用户信息

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private Integer gender;
    }
}
