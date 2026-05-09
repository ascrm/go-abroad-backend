package com.goAbroad.auth.dto;

import lombok.Data;

/**
 * 用户信息更新请求
 */
@Data
public class UserInfoUpdateRequest {

    private String nickname;

    private String avatar;

    private Integer gender;

    private String birthday;

    private String bio;
}
