package com.goAbroad.auth.controller;

import com.goAbroad.auth.dto.LoginRequest;
import com.goAbroad.auth.dto.LoginResponse;
import com.goAbroad.auth.dto.RegisterRequest;
import com.goAbroad.auth.dto.SendCodeRequest;
import com.goAbroad.auth.dto.SendCodeResponse;
import com.goAbroad.auth.dto.SocialLoginRequest;
import com.goAbroad.auth.dto.SocialRegisterRequest;
import com.goAbroad.auth.service.AuthServiceImpl;
import com.goAbroad.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthServiceImpl authService;

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
     * 第三方注册（Google/Apple）
     */
    @PostMapping("/social/register")
    public R<LoginResponse> socialRegister(@Valid @RequestBody SocialRegisterRequest request) {
        LoginResponse response = authService.socialRegister(request);
        return R.ok(response);
    }

    /**
     * 第三方登录
     * 支持微信、QQ、Google、Apple、抖音
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
}
