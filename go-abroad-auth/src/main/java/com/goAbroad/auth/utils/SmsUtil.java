package com.goAbroad.auth.utils;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.goAbroad.common.exception.BusinessException;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 短信发送服务
 * 使用阿里云短信服务（新版本SDK）
 */
@Service
@Slf4j
public class SmsUtil {

    @Value("${app.sms.enabled:false}")
    private boolean enabled;

    @Value("${app.sms.signature:GoAbroad}")
    private String signature;

    @Value("${app.sms.template-code:}")
    private String templateCode;

    @Value("${app.sms.region:ap-southeast-1}")
    private String region;

    @Value("${app.sms.endpoint:dypnsapi.aliyuncs.com}")
    private String endpoint;

    /**
     * 发送验证码短信
     *
     * @param phone   收件人手机号
     * @param code    验证码
     * @param codeType 验证码类型
     */
    public void sendCaptchaSms(String phone, String code, Integer codeType) {
        if (!enabled) {
            log.warn("短信发送已禁用，跳过发送到: {}", maskPhone(phone));
            return;
        }

        // 从环境变量读取阿里云 AccessKey
        String accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");

        if (accessKeyId == null || accessKeyId.isEmpty() || accessKeySecret == null || accessKeySecret.isEmpty()) {
            log.error("阿里云 AccessKey 未配置");
            throw new BusinessException("短信服务配置错误，请联系管理员");
        }

        try {
            StaticCredentialProvider provider = StaticCredentialProvider.create(
                    Credential.builder()
                            .accessKeyId(accessKeyId)
                            .accessKeySecret(accessKeySecret)
                            .build()
            );

            // 构建 AsyncClient
            try (AsyncClient client = AsyncClient.builder()
                    .region(region)
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                            ClientOverrideConfiguration.create()
                                    .setEndpointOverride(endpoint)
                    )
                    .build()) {

            // 构建请求
            String param = String.format("{\"code\":\"%s\",\"min\":\"5\"}", code);
            SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
                    .signName(signature)
                    .templateCode(templateCode)
                    .templateParam(param)
                    .phoneNumber(phone)
                    .build();

                // 发送短信
                CompletableFuture<SendSmsVerifyCodeResponse> response = client.sendSmsVerifyCode(request);
                SendSmsVerifyCodeResponse resp = response.get();

                if ("OK".equals(resp.getBody().getCode())) {
                    log.info("验证码短信发送成功: {}", maskPhone(phone));
                } else {
                    log.error("验证码短信发送失败: {}, code: {}, message: {}",
                            maskPhone(phone), resp.getBody().getCode(), resp.getBody().getMessage());
                    throw new BusinessException("短信发送失败: " + resp.getBody().getMessage());
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证码短信发送异常: {}, error: {}", maskPhone(phone), e.getMessage());
            throw new BusinessException("短信发送失败，请稍后重试");
        }
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
