package com.goAbroad.core.profile.controller;

import com.goAbroad.auth.repository.UserRepository;
import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import com.goAbroad.core.community.dto.ArticleResponse;
import com.goAbroad.core.community.dto.QuestionResponse;
import com.goAbroad.core.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class BrowseHistoryController {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    /**
     * 获取我创建的文章
     */
    @GetMapping("/my-articles")
    public R<List<ArticleResponse>> getMyArticles() {
        Long userId = UserHolder.getUserId();
        return R.ok(profileService.getMyArticles(userId));
    }

    /**
     * 获取我收藏的文章
     */
    @GetMapping("/my-favorite-articles")
    public R<List<ArticleResponse>> getMyFavoriteArticles() {
        Long userId = UserHolder.getUserId();
        return R.ok(profileService.getMyFavoriteArticles(userId));
    }

    /**
     * 获取我浏览过的文章
     */
    @GetMapping("/my-browsed-articles")
    public R<List<ArticleResponse>> getMyBrowsedArticles() {
        Long userId = UserHolder.getUserId();
        return R.ok(profileService.getMyBrowsedArticles(userId));
    }

    /**
     * 获取我浏览过的问答
     */
    @GetMapping("/my-browsed-questions")
    public R<List<QuestionResponse>> getMyBrowsedQuestions() {
        Long userId = UserHolder.getUserId();
        return R.ok(profileService.getMyBrowsedQuestions(userId));
    }
}