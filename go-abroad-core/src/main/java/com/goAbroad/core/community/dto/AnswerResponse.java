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
public class AnswerResponse {
    private Long id;
    private Long questionId;
    private Long authorId;
    private AuthorDTO author;
    private String content;
    private Integer likes;
    private Integer repliesCount;
    private Boolean isOfficial;
    private Boolean isBestAnswer;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isLiked;
}
