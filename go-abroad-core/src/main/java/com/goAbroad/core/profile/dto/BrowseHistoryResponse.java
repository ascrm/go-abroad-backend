package com.goAbroad.core.profile.dto;

import lombok.Data;

@Data
public class BrowseHistoryResponse {
    private Long id;
    private String title;
    private String author;
    private String views;
    private String thumbnailUrl;
    private String sourceType;
    private Long sourceId;
}