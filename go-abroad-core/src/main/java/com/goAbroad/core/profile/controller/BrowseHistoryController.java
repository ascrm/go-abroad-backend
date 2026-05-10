package com.goAbroad.core.profile.controller;

import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import com.goAbroad.core.profile.dto.BrowseHistoryResponse;
import com.goAbroad.core.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class BrowseHistoryController {

    private final ProfileService profileService;

    @GetMapping("/browse-history")
    public R<List<BrowseHistoryResponse>> getBrowseHistory() {
        Long userId = UserHolder.getUserId();
        return R.ok(profileService.getBrowseHistory(userId));
    }
}