package com.goAbroad.auth.utils;

import com.goAbroad.auth.entity.UserAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 验证码服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaUtil {

    private final StringRedisTemplate redisTemplate;
    private final EmailUtil emailUtil;
    private final SmsUtil smsUtil;

    /**
     * Redis 验证码 key 前缀
     */
    private static final String CAPTCHA_PREFIX = "captcha:";

    /**
     * 验证码过期时间（秒）
     */
    private static final long CAPTCHA_EXPIRE_SECONDS = 300; // 5分钟

    /**
     * 验证码长度
     */
    private static final int CAPTCHA_LENGTH = 6;

    /**
     * 生成验证码
     *
     * @param accountType 账号类型: 2-邮箱, 3-手机号
     * @param account     账号
     * @param codeType    验证码类型: 1-注册, 2-登录, 3-找回密码
     */
    public void generateCaptcha(Integer accountType, String account, Integer codeType) {
        // 生成6位数字验证码
        String code = generateRandomCode();

        // 生成验证码ID
        String codeId = UUID.randomUUID().toString().replace("-", "");

        // Redis key: captcha:{accountType}:{codeType}:{account}
        String key = buildKey(accountType, codeType, account);

        // 存储验证码到 Redis
        redisTemplate.opsForValue().set(key, code, Duration.ofSeconds(CAPTCHA_EXPIRE_SECONDS));

        // 发送验证码到邮箱或手机
        sendCaptcha(accountType, account, code, codeType);

        log.info("生成验证码: accountType={}, codeType={}, account={}", accountType, codeType, maskAccount(account));

    }

    /**
     * 发送验证码到邮箱或手机
     */
    private void sendCaptcha(Integer accountType, String account, String code, Integer codeType) {
        if (accountType == UserAccount.AccountType.EMAIL) {
            // 发送邮箱验证码
            emailUtil.sendCaptchaEmail(account, code, codeType);
        } else if (accountType == UserAccount.AccountType.PHONE) {
            // 发送短信验证码
            smsUtil.sendCaptchaSms(account, code, codeType);
        } else {
            log.warn("不支持的账号类型: {}", accountType);
        }
    }

    /**
     * 验证验证码
     *
     * @param accountType 账号类型: 2-邮箱, 3-手机号
     * @param account     账号
     * @param codeType    验证码类型: 1-注册, 2-登录, 3-找回密码
     * @param code        用户输入的验证码
     * @return 是否验证成功
     */
    public boolean verifyCaptcha(Integer accountType, String account, Integer codeType, String code) {
        String key = buildKey(accountType, codeType, account);

        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("验证码已过期或不存在: accountType={}, codeType={}, account={}",
                    accountType, codeType, maskAccount(account));
            return false;
        }

        boolean matched = storedCode.equals(code);

        if (matched) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            log.info("验证码验证成功: accountType={}, codeType={}, account={}",
                    accountType, codeType, maskAccount(account));
        } else {
            log.warn("验证码错误: accountType={}, codeType={}, account={}, input={}",
                    accountType, codeType, maskAccount(account), code);
        }

        return matched;
    }

    /**
     * 获取验证码（开发环境使用）
     *
     * @param accountType 账号类型
     * @param account     账号
     * @param codeType    验证码类型
     * @return 验证码
     */
    public String getCaptcha(Integer accountType, String account, Integer codeType) {
        String key = buildKey(accountType, codeType, account);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 构建 Redis key
     */
    private String buildKey(Integer accountType, Integer codeType, String account) {
        return CAPTCHA_PREFIX + accountType + ":" + codeType + ":" + account;
    }

    /**
     * 生成随机6位数字验证码
     */
    private String generateRandomCode() {
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        return String.valueOf(code);
    }

    /**
     * 账号脱敏
     */
    private String maskAccount(String account) {
        if (account == null) {
            return "";
        }
        if (account.contains("@")) {
            // 邮箱脱敏
            int atIndex = account.indexOf("@");
            if (atIndex > 2) {
                return account.substring(0, 2) + "***" + account.substring(atIndex);
            }
            return "***" + account.substring(atIndex);
        } else if (account.length() > 7) {
            // 手机号脱敏
            return account.substring(0, 3) + "****" + account.substring(7);
        }
        return "***";
    }

    /**
     * 获取验证码过期时间（秒）
     */
    public long getExpireSeconds() {
        return CAPTCHA_EXPIRE_SECONDS;
    }
}
