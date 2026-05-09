package com.goAbroad.core.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private Long id;
    private String title;
    private Long authorId;
    private AuthorDTO author;
    private String category;
    private Integer views;
    private Integer repliesCount;
    private Boolean isResolved;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isFavorited;
    /** 点赞最高的回答摘要 */
    private TopAnswer topAnswer;
    /** 是否有回答 */
    private Boolean hasAnswers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopAnswer {
        /** 作者信息 */
        private AuthorDTO author;
        /** 内容摘要 */
        private String content;
        /** 点赞数 */
        private Integer likes;
        /** 评论数 */
        private Integer repliesCount;
    }
}
