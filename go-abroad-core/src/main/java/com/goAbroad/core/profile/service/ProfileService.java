package com.goAbroad.core.profile.service;

import com.goAbroad.core.profile.dto.BrowseHistoryResponse;
import com.goAbroad.core.profile.entity.BrowseHistory;
import com.goAbroad.core.profile.repository.BrowseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final BrowseHistoryRepository browseHistoryRepository;

    public List<BrowseHistoryResponse> getBrowseHistory(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<BrowseHistory> histories = browseHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return histories.stream()
                .map(entity -> {
                    BrowseHistoryResponse response = new BrowseHistoryResponse();
                    response.setId(entity.getId());
                    response.setTitle(entity.getTitle());
                    response.setAuthor(entity.getAuthor());
                    response.setViews(entity.getViews());
                    response.setThumbnailUrl(entity.getThumbnailUrl());
                    response.setSourceType(entity.getSourceType());
                    response.setSourceId(entity.getSourceId());
                    return response;
                })
                .collect(Collectors.toList());
    }
}