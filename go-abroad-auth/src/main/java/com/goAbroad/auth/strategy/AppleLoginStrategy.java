package com.goAbroad.auth.strategy;

import com.goAbroad.auth.dto.SocialLoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Apple 登录策略实现
 * TODO: 完善 Apple 登录逻辑
 */
@Slf4j
@Component
public class AppleLoginStrategy implements SocialLoginStrategy {

    @Override
    public int getSocialType() {
        return 4; // Apple
    }

    @Override
    public SocialUserInfo processLogin(SocialLoginRequest request) {
        log.warn("Apple 登录功能尚未完善，当前为占位实现");
        // TODO: Apple 登录完整实现
        // Apple 登录使用 Sign in with Apple，需要验证 identityToken
        // 1. 验证 id_token（JWT）获取 user 标识
        // 2. 返回 SocialUserInfo

        throw new UnsupportedOperationException("Apple 登录功能尚未完善");
    }
}
