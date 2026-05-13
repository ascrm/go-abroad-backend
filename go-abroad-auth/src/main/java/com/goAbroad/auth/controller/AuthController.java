package com.goAbroad.auth.controller;

import com.goAbroad.auth.dto.*;
import com.goAbroad.auth.service.AuthServiceImpl;
import com.goAbroad.auth.utils.JwtUtils;
import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthServiceImpl authService;
    private final JwtUtils jwtUtils;

    /**
     * 账号密码登录
     * 支持用户名、邮箱、手机号登录
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return R.ok(response);
    }

    /**
     * 发送验证码
     * 用于注册、登录、找回密码等场景
     */
    @PostMapping("/sendCode")
    public R<SendCodeResponse> sendCode(@Valid @RequestBody SendCodeRequest request) {
        SendCodeResponse response = authService.sendCode(request);
        return R.ok(response);
    }

    /**
     * 邮箱/手机号注册
     */
    @PostMapping("/register")
    public R<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return R.ok(response);
    }

    /**
     * 第三方登录
     * 支持微信、QQ、Google、Apple、抖音
     * 第一次登录时自动创建账号
     */
    @PostMapping("/social/login")
    public R<LoginResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        LoginResponse response = authService.socialLogin(request);
        return R.ok(response);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return R.ok(response);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        // 优先从 UserHolder 获取，否则从 token 解析
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            userId = getUserIdFromToken(request);
        }
        if (userId != null) {
            authService.logout(userId);
            UserHolder.clear();
        }
        return R.ok();
    }

    /**
     * 切换账号
     */
    @PostMapping("/switch-account")
    public R<LoginResponse> switchAccount(@RequestBody SwitchAccountRequest request) {
        LoginResponse response = authService.switchAccount(request.getAccountType(), request.getAccountValue());
        return R.ok(response);
    }

    /**
     * 修改密码（已登录用户）
     */
    @PostMapping("/reset-password")
    public R<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getNewPassword());
        return R.ok();
    }

    /**
     * 通过验证码重置密码（忘记密码）
     */
    @PostMapping("/reset-password-by-code")
    public R<Void> resetPasswordByCode(@Valid @RequestBody ResetPasswordByCodeRequest request) {
        authService.resetPasswordByCode(
                request.getAccountType(),
                request.getAccountValue(),
                request.getCode(),
                request.getNewPassword()
        );
        return R.ok();
    }

    /**
     * 验证账号是否属于当前用户（用于找回密码）
     */
    @PostMapping("/verify-account")
    public R<Void> verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
        authService.verifyAccountBelongsToCurrentUser(request.getAccountType(), request.getAccountValue());
        return R.ok();
    }

    /**
     * 验证验证码是否正确（用于找回密码）
     */
    @PostMapping("/verify-code")
    public R<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request.getAccountType(), request.getAccount(), request.getCode());
        return R.ok();
    }

    /**
     * 从 Token 中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (jwtUtils.validateToken(token)) {
                return jwtUtils.getUserId(token);
            }
        }
        return null;
    }
}
