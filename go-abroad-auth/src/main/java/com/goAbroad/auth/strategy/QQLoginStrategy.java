package com.goAbroad.auth.strategy;

import com.goAbroad.auth.dto.SocialLoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * QQ 登录策略实现
 * TODO: 完善 QQ 登录逻辑
 */
@Slf4j
@Component
public class QQLoginStrategy implements SocialLoginStrategy {

    @Override
    public int getSocialType() {
        return 2; // QQ
    }

    @Override
    public SocialUserInfo processLogin(SocialLoginRequest request) {
        log.warn("QQ 登录功能尚未完善，当前为占位实现");
        // TODO: QQ 登录完整实现
        // 1. 用 code 换 access_token
        // 2. 用 access_token 获取 openid（需要二次请求）
        // 3. 用 openid + access_token 获取用户信息
        // 4. 返回 SocialUserInfo

        throw new UnsupportedOperationException("QQ 登录功能尚未完善");
    }
}
