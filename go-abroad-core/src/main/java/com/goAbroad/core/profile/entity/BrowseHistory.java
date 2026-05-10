package com.goAbroad.core.profile.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_browse_history")
public class BrowseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private String author;

    private String views;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}