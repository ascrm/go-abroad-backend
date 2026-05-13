package com.goAbroad.core.search.dto;

import lombok.*;

/**
 * 搜索结果DTO - 用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
}