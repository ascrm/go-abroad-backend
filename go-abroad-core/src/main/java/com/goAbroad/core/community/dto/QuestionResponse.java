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
    private String content;
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
}
