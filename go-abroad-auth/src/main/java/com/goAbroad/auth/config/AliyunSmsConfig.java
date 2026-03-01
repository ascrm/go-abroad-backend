package com.goAbroad.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云短信配置（新版本SDK）
 * Client 现在在 SmsService 中直接创建
 */
@Slf4j
@Configuration
public class AliyunSmsConfig {
    // 新版本SDK不需要在此配置Client，SmsService中直接创建
    // 凭证通过 DefaultCredentialProvider 从环境变量自动获取
}
