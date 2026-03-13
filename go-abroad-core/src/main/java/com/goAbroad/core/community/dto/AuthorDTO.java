package com.goAbroad.core.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
}
