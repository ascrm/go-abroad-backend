package com.goAbroad.core.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    /** 评论ID */
    private Long id;
    /** 所属回答ID */
    private Long answerId;
    /** 父评论ID */
    private Long parentId;
    /** 作者信息 */
    private AuthorDTO author;
    /** 评论内容 */
    private String content;
    /** 点赞数 */
    private Integer likes;
    /** 子评论数量 */
    private Integer repliesCount;
    /** 子评论列表 */
    private List<CommentResponse> replies;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 是否已点赞 */
    private Boolean isLiked;
}