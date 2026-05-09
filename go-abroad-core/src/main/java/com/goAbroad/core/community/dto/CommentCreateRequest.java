package com.goAbroad.core.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    /** 所属回答ID */
    private Long answerId;
    /** 父评论ID，NULL表示顶层评论 */
    private Long parentId;
    /** 评论内容 */
    private String content;
}