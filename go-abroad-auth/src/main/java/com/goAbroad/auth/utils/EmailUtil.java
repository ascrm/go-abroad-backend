package com.goAbroad.auth.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailUtil {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean enabled;

    @Value("${app.email.signature:GoAbroad}")
    private String signature;

    /**
     * 发送验证码邮件
     *
     * @param to      收件人邮箱
     * @param code    验证码
     * @param codeType 验证码类型（注册、登录、找回密码）
     */
    public void sendCaptchaEmail(String to, String code, Integer codeType) {
        if (!enabled) {
            log.warn("邮件发送已禁用，跳过发送到: {}", maskEmail(to));
            return;
        }

        try {
            String typeName = getCodeTypeName(codeType);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("【" + signature + "】您的" + typeName + "验证码");
            message.setText(buildEmailContent(code, typeName));

            mailSender.send(message);
            log.info("验证码邮件发送成功: {}", maskEmail(to));
        } catch (Exception e) {
            log.error("验证码邮件发送失败: {}, error: {}", maskEmail(to), e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 获取验证码类型名称
     */
    private String getCodeTypeName(Integer codeType) {
        return switch (codeType) {
            case 1 -> "注册";
            case 2 -> "登录";
            case 3 -> "找回密码";
            default -> "验证";
        };
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, String typeName) {
        return "尊敬的用户：\n\n" +
                "您的" + typeName + "验证码为：\n\n" +
                "【" + code + "】\n\n" +
                "验证码有效期为5分钟，请尽快完成验证。\n\n" +
                "如非本人操作，请忽略此邮件。\n\n" +
                "此邮件由系统自动发送，请勿回复。\n\n" +
                "--\n" +
                signature;
    }

    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex > 2) {
            return email.substring(0, 2) + "***" + email.substring(atIndex);
        }
        return "***" + email.substring(atIndex);
    }
}
