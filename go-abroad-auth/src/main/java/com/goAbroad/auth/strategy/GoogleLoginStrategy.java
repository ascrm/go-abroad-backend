package com.goAbroad.auth.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goAbroad.auth.dto.SocialLoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Google 登录策略实现
 * 完整实现：code -> access_token -> userinfo -> 获取唯一标识
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleLoginStrategy implements SocialLoginStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Google OAuth 配置（从 application.yml 读取）
    @Value("${app.social.google.client-id:}")
    private String clientId;

    @Value("${app.social.google.redirect-uri:}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Override
    public int getSocialType() {
        return 3; // Google
    }

    @Override
    public SocialUserInfo processLogin(SocialLoginRequest request) {
        String code = request.getCode();

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Google 授权码不能为空");
        }

        log.info("Google 登录：开始用授权码换取 access_token");

        // 第一步：用 code 换 access_token
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("code", code);
        tokenParams.put("client_id", clientId);
        tokenParams.put("redirect_uri", redirectUri);
        tokenParams.put("grant_type", "authorization_code");

        String tokenResponse = sendPostRequest(tokenParams);
        JsonNode tokenJson = parseJson(tokenResponse);

        String accessToken = tokenJson.has("access_token") ? tokenJson.get("access_token").asText() : null;
        String refreshToken = tokenJson.has("refresh_token") ? tokenJson.get("refresh_token").asText() : null;
        Long expiresIn = tokenJson.has("expires_in") ? tokenJson.get("expires_in").asLong() : null;

        if (accessToken == null) {
            log.error("Google 登录：换取 access_token 失败，响应：{}", tokenResponse);
            throw new IllegalStateException("Google 授权码换取 access_token 失败");
        }

        log.info("Google 登录：成功获取 access_token");

        // 第二步：用 access_token 获取用户信息
        String userInfoResponse = sendGetRequest(USERINFO_URL + "?access_token=" + accessToken);
        JsonNode userInfoJson = parseJson(userInfoResponse);

        // Google 返回的 id 就是唯一标识（不是 email，email 可能变化）
        String openid = userInfoJson.has("id") ? userInfoJson.get("id").asText() : null;
        String nickname = userInfoJson.has("name") ? userInfoJson.get("name").asText() : null;
        String avatar = userInfoJson.has("picture") ? userInfoJson.get("picture").asText() : null;
        String email = userInfoJson.has("email") ? userInfoJson.get("email").asText() : null;

        // 性别处理：Google API 不直接返回性别，用 -1 表示未知
        Integer gender = 0;

        if (openid == null) {
            log.error("Google 登录：获取用户信息失败，响应：{}", userInfoResponse);
            throw new IllegalStateException("获取 Google 用户信息失败");
        }

        log.info("Google 登录：成功获取用户信息，openid={}, nickname={}", openid, nickname);

        // 构建结果
        SocialUserInfo userInfo = new SocialUserInfo(openid);
        userInfo.setNickname(nickname);
        userInfo.setAvatar(avatar);
        userInfo.setGender(gender);
        userInfo.setAccessToken(accessToken);
        userInfo.setRefreshToken(refreshToken);
        userInfo.setExpiresIn(expiresIn);

        return userInfo;
    }

    private String sendPostRequest(Map<String, String> params) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 使用传统方式构建表单参数（兼容 Java 8）
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", params.get("code"));
        body.add("client_id", params.get("client_id"));
        body.add("client_secret", params.get("client_secret"));
        body.add("redirect_uri", params.get("redirect_uri"));
        body.add("grant_type", params.get("grant_type"));

        HttpEntity<LinkedMultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(GoogleLoginStrategy.TOKEN_URL, request, String.class);
    }

    private String sendGetRequest(String url) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("解析 JSON 失败: " + json, e);
        }
    }
}
