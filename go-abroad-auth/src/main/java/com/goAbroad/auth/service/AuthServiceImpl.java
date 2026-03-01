package com.goAbroad.auth.service;

import com.goAbroad.auth.utils.JwtUtils;
import com.goAbroad.auth.dto.*;
import com.goAbroad.auth.entity.User;
import com.goAbroad.auth.entity.UserAccount;
import com.goAbroad.auth.entity.UserSocial;
import com.goAbroad.auth.repository.UserAccountRepository;
import com.goAbroad.auth.repository.UserRepository;
import com.goAbroad.auth.repository.UserSocialRepository;
import com.goAbroad.auth.utils.CaptchaUtil;
import com.goAbroad.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserSocialRepository userSocialRepository;
    private final JwtUtils jwtUtils;
    private final CaptchaUtil captchaUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 账号密码登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();

        // 1. 判断账号类型
        Integer accountType = getAccountType(account);

        // 2. 查询账号
        UserAccount userAccount = userAccountRepository.findByAccountTypeAndAccountValue(accountType, account)
                .orElseThrow(() -> new BusinessException("账号或密码错误"));

        // 3. 验证密码
        if (!passwordEncoder.matches(password, userAccount.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        // 4. 检查用户状态
        User user = userAccount.getUser();
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 5. 生成 Token
        return buildLoginResponse(user);
    }

    /**
     * 发送验证码
     */
    public SendCodeResponse sendCode(SendCodeRequest request) {
        Integer accountType = request.getAccountType();
        String account = request.getAccount();
        Integer codeType = request.getCodeType();

        // 1. 验证账号格式
        if (accountType == UserAccount.AccountType.EMAIL) {
            if (!isEmail(account)) {
                throw new BusinessException("邮箱格式不正确");
            }
        } else if (accountType == UserAccount.AccountType.PHONE) {
            if (!isPhone(account)) {
                throw new BusinessException("手机号格式不正确");
            }
        } else {
            throw new BusinessException("不支持的账号类型");
        }

        // 2. 验证码类型校验
        // 注册时检查账号是否已存在
        if (codeType == 1) { // 注册
            if (userAccountRepository.findByAccountTypeAndAccountValue(accountType, account).isPresent()) {
                throw new BusinessException("该账号已被注册");
            }
        }

        // 3. 生成验证码
        captchaUtil.generateCaptcha(accountType, account, codeType);

        // 4. 构建响应（开发环境返回验证码，生产环境不返回）
        String code = captchaUtil.getCaptcha(accountType, account, codeType);

        return SendCodeResponse.builder()
                .codeId("") // 暂时不需要，后续可用于限流等
                .code(code) // 开发环境返回
                .expiresIn(captchaUtil.getExpireSeconds())
                .target(maskAccount(account))
                .build();
    }

    /**
     * 邮箱/手机号注册
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();
        String code = request.getCode();

        // 1. 判断账号类型
        Integer accountType = request.getAccountType();
        if (accountType == null) {
            accountType = getAccountType(account);
        }

        // 2. 校验验证码
        boolean verified = captchaUtil.verifyCaptcha(accountType, account, 1, code); // 1=注册
        if (!verified) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 3. 检查账号是否已存在
        if (userAccountRepository.findByAccountTypeAndAccountValue(accountType, account).isPresent()) {
            throw new BusinessException("该账号已被注册");
        }

        // 4. 生成随机用户名
        String username = generateRandomUsername(accountType, account);

        // 5. 创建用户
        User user = User.builder()
                .username(username)
                .nickname(accountType == UserAccount.AccountType.EMAIL ?
                        account.substring(0, account.indexOf("@")) : null)
                .status(1)
                .build();
        user = userRepository.save(user);

        // 6. 创建账号记录
        UserAccount userAccount = UserAccount.builder()
                .user(user)
                .accountType(accountType)
                .accountValue(account)
                .password(passwordEncoder.encode(password))
                .verified(true)
                .build();
        userAccountRepository.save(userAccount);

        // 7. 生成 Token
        return buildLoginResponse(user);
    }

    /**
     * 生成随机用户名
     */
    private String generateRandomUsername(Integer accountType, String account) {
        String prefix;
        if (accountType == UserAccount.AccountType.EMAIL) {
            prefix = "user";
        } else if (accountType == UserAccount.AccountType.PHONE) {
            prefix = "phone";
        } else {
            prefix = "user";
        }

        // 生成8位随机数字
        Random random = new Random();
        int randomNum = 10000000 + random.nextInt(90000000);

        return prefix + randomNum;
    }

    /**
     * 第三方注册（Google/Apple）
     */
    @Transactional
    public LoginResponse socialRegister(SocialRegisterRequest request) {
        Integer socialType = request.getSocialType();
        String openid = request.getOpenid();

        // 1. 检查是否已注册
        if (userSocialRepository.findBySocialTypeAndOpenid(socialType, openid).isPresent()) {
            throw new BusinessException("该第三方账号已被注册");
        }

        // 2. 验证支持的平台
        if (socialType != UserSocial.SocialType.GOOGLE && socialType != UserSocial.SocialType.APPLE) {
            throw new BusinessException("不支持的第三方平台");
        }

        // 3. 生成随机用户名
        String username = generateRandomUsernameForSocial(socialType);

        // 4. 创建用户
        User user = User.builder()
                .username(username)
                .nickname(request.getNickname())
                .avatar(request.getAvatar())
                .gender(request.getGender() != null ? request.getGender() : 0)
                .status(1)
                .build();
        user = userRepository.save(user);

        // 5. 创建第三方登录记录
        UserSocial userSocial = UserSocial.builder()
                .user(user)
                .socialType(socialType)
                .openid(openid)
                .unionid(request.getUnionid())
                .accessToken(request.getAccessToken())
                .refreshToken(request.getRefreshToken())
                .expiresAt(request.getExpiresIn() != null ?
                        LocalDateTime.now().plusSeconds(request.getExpiresIn()) : null)
                .build();
        userSocialRepository.save(userSocial);

        // 6. 生成 Token
        return buildLoginResponse(user);
    }

    /**
     * 为第三方用户生成随机用户名
     */
    private String generateRandomUsernameForSocial(Integer socialType) {
        String prefix;
        if (socialType == UserSocial.SocialType.GOOGLE) {
            prefix = "google";
        } else if (socialType == UserSocial.SocialType.APPLE) {
            prefix = "apple";
        } else {
            prefix = "social";
        }

        Random random = new Random();
        int randomNum = 10000000 + random.nextInt(90000000);

        return prefix + randomNum;
    }

    /**
     * 第三方登录
     */
    @Transactional
    public LoginResponse socialLogin(SocialLoginRequest request) {
        Integer socialType = request.getSocialType();
        String openid = request.getOpenid();

        // 1. 查询是否已绑定
        UserSocial userSocial = userSocialRepository.findBySocialTypeAndOpenid(socialType, openid)
                .orElse(null);

        User user;

        if (userSocial != null) {
            // 已绑定，直接登录
            user = userSocial.getUser();
            if (user.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }

            // 更新第三方登录信息
            updateSocialInfo(userSocial, request);
        } else {
            // 未绑定，创建新用户或绑定到已有用户
            user = createOrBindSocialUser(request);
        }

        // 生成 Token
        return buildLoginResponse(user);
    }

    /**
     * 创建或绑定第三方用户
     */
    @Transactional
    public User createOrBindSocialUser(SocialLoginRequest request) {
        // TODO: 如果需要绑定到已有用户，这里需要额外的逻辑
        // 目前创建新用户
        User user = User.builder()
                .username("social_" + request.getOpenid().substring(0, 8))
                .nickname(request.getNickname())
                .avatar(request.getAvatar())
                .gender(request.getGender() != null ? request.getGender() : 0)
                .status(1)
                .build();
        user = userRepository.save(user);

        // 创建第三方登录记录
        UserSocial userSocial = UserSocial.builder()
                .user(user)
                .socialType(request.getSocialType())
                .openid(request.getOpenid())
                .unionid(request.getUnionid())
                .accessToken(request.getAccessToken())
                .refreshToken(request.getRefreshToken())
                .expiresAt(request.getExpiresIn() != null ? 
                        LocalDateTime.now().plusSeconds(request.getExpiresIn()) : null)
                .build();
        userSocialRepository.save(userSocial);

        return user;
    }

    /**
     * 更新第三方登录信息
     */
    private void updateSocialInfo(UserSocial userSocial, SocialLoginRequest request) {
        if (request.getAccessToken() != null) {
            userSocial.setAccessToken(request.getAccessToken());
        }
        if (request.getRefreshToken() != null) {
            userSocial.setRefreshToken(request.getRefreshToken());
        }
        if (request.getExpiresIn() != null) {
            userSocial.setExpiresAt(LocalDateTime.now().plusSeconds(request.getExpiresIn()));
        }
        userSocialRepository.save(userSocial);
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(User user) {
        String username = user.getUsername() != null ? user.getUsername() : "";

        String accessToken = jwtUtils.generateAccessToken(user.getId(), username);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), username);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .user(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .gender(user.getGender())
                        .build())
                .build();
    }

    /**
     * 根据账号判断类型
     */
    private Integer getAccountType(String account) {
        if (isEmail(account)) {
            return UserAccount.AccountType.EMAIL;
        } else if (isPhone(account)) {
            return UserAccount.AccountType.PHONE;
        } else {
            return UserAccount.AccountType.USERNAME;
        }
    }

    /**
     * 判断是否为邮箱
     */
    private boolean isEmail(String account) {
        Matcher matcher = EMAIL_PATTERN.matcher(account);
        return matcher.matches();
    }

    /**
     * 判断是否为手机号
     */
    private boolean isPhone(String account) {
        Matcher matcher = PHONE_PATTERN.matcher(account);
        return matcher.matches();
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
     * 刷新 Token
     */
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException("无效的刷新令牌");
        }

        Long userId = jwtUtils.getUserId(refreshToken);
        String username = jwtUtils.getUsername(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        return buildLoginResponse(user);
    }
}
