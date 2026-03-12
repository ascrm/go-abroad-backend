package com.goAbroad.auth.strategy;

import com.goAbroad.auth.dto.SocialLoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 微信登录策略实现
 * TODO: 完善微信登录逻辑（需要 appid, appsecret 等配置）
 */
@Slf4j
@Component
public class WeChatLoginStrategy implements SocialLoginStrategy {

    @Override
    public int getSocialType() {
        return 1; // 微信
    }

    @Override
    public SocialUserInfo processLogin(SocialLoginRequest request) {
        log.warn("微信登录功能尚未完善，当前为占位实现");
        // TODO: 微信登录完整实现
        // 1. 用 code 换 access_token（微信使用 OAuth2.0 授权登录）
        // 2. 用 access_token 获取用户信息
        // 3. 返回 SocialUserInfo

        throw new UnsupportedOperationException("微信登录功能尚未完善");
    }
}
