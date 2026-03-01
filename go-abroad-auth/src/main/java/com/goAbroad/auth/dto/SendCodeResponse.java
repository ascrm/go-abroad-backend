package com.goAbroad.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeResponse {

    /**
     * 验证码ID（用于后续验证）
     */
    private String codeId;

    /**
     * 验证码（开发环境返回，生产环境不返回）
     */
    private String code;

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 目标（邮箱或手机号掩码）
     */
    private String target;
}
