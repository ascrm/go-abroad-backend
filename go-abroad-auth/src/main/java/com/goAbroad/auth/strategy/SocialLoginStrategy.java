package com.goAbroad.auth.strategy;

import com.goAbroad.auth.dto.SocialLoginRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * 第三方登录策略接口
 * 使用策略模式支持不同平台的登录逻辑
 */
public interface SocialLoginStrategy {

    /**
     * 获取支持的平台类型
     * @return 平台类型枚举值（1-微信,2-QQ,3-Google,4-Apple,5-抖音）
     */
    int getSocialType();

    /**
     * 处理第三方登录
     * @param request 登录请求（可能只包含授权码）
     * @return 解析后的用户信息（包含openid和可选的用户信息）
     */
    SocialUserInfo processLogin(SocialLoginRequest request);

    /**
     * 第三方用户信息
     */
    @Setter
    @Getter
    class SocialUserInfo {
        // Getters and Setters
        private String openid;          // 第三方平台唯一标识
        private String unionid;         // 开放平台唯一标识（微信/QQ）
        private String nickname;        // 昵称
        private String avatar;          // 头像
        private Integer gender;         // 性别
        private String accessToken;     // 访问令牌
        private String refreshToken;    // 刷新令牌
        private Long expiresIn;         // 有效期（秒）

        public SocialUserInfo(String openid) {
            this.openid = openid;
        }

    }
}
