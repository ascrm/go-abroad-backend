package com.goAbroad.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private Integer gender;

    private String birthday;

    private String bio;

    private Integer status;
}
