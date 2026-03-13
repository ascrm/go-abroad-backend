package com.goAbroad.core.community.controller;

import com.goAbroad.common.result.PageR;
import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import com.goAbroad.core.community.dto.*;
import com.goAbroad.core.community.service.CommunityServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityServiceImpl communityService;

    @GetMapping("/article/list")
    public R<PageR<ArticleResponse>> getArticleList(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageR<ArticleResponse> result = communityService.getArticleList(tag, isFeatured, page, pageSize);
        return R.ok(result);
    }

    @GetMapping("/article/{id}")
    public R<ArticleResponse> getArticleDetail(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        ArticleResponse result = communityService.getArticleDetail(userId, id);
        return R.ok(result);
    }

    @PostMapping("/article")
    public R<ArticleResponse> createArticle(@RequestBody ArticleCreateRequest request) {
        Long userId = UserHolder.getUserId();
        ArticleResponse result = communityService.createArticle(userId, request);
        return R.ok(result);
    }

    @PutMapping("/article/{id}")
    public R<ArticleResponse> updateArticle(@PathVariable Long id, @RequestBody ArticleUpdateRequest request) {
        Long userId = UserHolder.getUserId();
        ArticleResponse result = communityService.updateArticle(userId, id, request);
        return R.ok(result);
    }

    @DeleteMapping("/article/{id}")
    public R<Void> deleteArticle(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        communityService.deleteArticle(userId, id);
        return R.ok();
    }

    @GetMapping("/question/list")
    public R<PageR<QuestionResponse>> getQuestionList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isResolved,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageR<QuestionResponse> result = communityService.getQuestionList(category, isResolved, page, pageSize);
        return R.ok(result);
    }

    @GetMapping("/question/{id}")
    public R<QuestionResponse> getQuestionDetail(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        QuestionResponse result = communityService.getQuestionDetail(userId, id);
        return R.ok(result);
    }

    @PostMapping("/question")
    public R<QuestionResponse> createQuestion(@RequestBody QuestionCreateRequest request) {
        Long userId = UserHolder.getUserId();
        QuestionResponse result = communityService.createQuestion(userId, request);
        return R.ok(result);
    }

    @PutMapping("/question/{id}")
    public R<QuestionResponse> updateQuestion(@PathVariable Long id, @RequestBody QuestionUpdateRequest request) {
        Long userId = UserHolder.getUserId();
        QuestionResponse result = communityService.updateQuestion(userId, id, request);
        return R.ok(result);
    }

    @DeleteMapping("/question/{id}")
    public R<Void> deleteQuestion(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        communityService.deleteQuestion(userId, id);
        return R.ok();
    }

    @GetMapping("/answer/list")
    public R<PageR<AnswerResponse>> getAnswerList(
            @RequestParam Long questionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageR<AnswerResponse> result = communityService.getAnswerList(questionId, page, pageSize);
        return R.ok(result);
    }

    @GetMapping("/answer/{id}")
    public R<AnswerResponse> getAnswerDetail(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        AnswerResponse result = communityService.getAnswerDetail(userId, id);
        return R.ok(result);
    }

    @PostMapping("/answer")
    public R<AnswerResponse> createAnswer(@RequestBody AnswerCreateRequest request) {
        Long userId = UserHolder.getUserId();
        AnswerResponse result = communityService.createAnswer(userId, request);
        return R.ok(result);
    }

    @PutMapping("/answer/{id}")
    public R<AnswerResponse> updateAnswer(@PathVariable Long id, @RequestBody AnswerUpdateRequest request) {
        Long userId = UserHolder.getUserId();
        AnswerResponse result = communityService.updateAnswer(userId, id, request);
        return R.ok(result);
    }

    @DeleteMapping("/answer/{id}")
    public R<Void> deleteAnswer(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        communityService.deleteAnswer(userId, id);
        return R.ok();
    }

    @PostMapping("/interaction/favorite")
    public R<InteractionResponse> handleFavorite(@RequestBody InteractionRequest request) {
        Long userId = UserHolder.getUserId();
        InteractionResponse result = communityService.handleFavorite(userId, request);
        return R.ok(result);
    }

    @PostMapping("/interaction/like")
    public R<InteractionResponse> handleLike(@RequestBody InteractionRequest request) {
        Long userId = UserHolder.getUserId();
        InteractionResponse result = communityService.handleLike(userId, request);
        return R.ok(result);
    }

    @PostMapping("/interaction/follow")
    public R<InteractionResponse> handleFollow(@RequestBody InteractionRequest request) {
        Long userId = UserHolder.getUserId();
        InteractionResponse result = communityService.handleFollow(userId, request);
        return R.ok(result);
    }

    @PostMapping("/interaction/view")
    public R<Void> handleView(@RequestBody InteractionRequest request) {
        Long userId = UserHolder.getUserId();
        communityService.handleView(userId, request);
        return R.ok();
    }

    @GetMapping("/interaction/check")
    public R<InteractionCheckResponse> checkInteractionStatus(
            @RequestParam Long targetId,
            @RequestParam String targetType) {
        Long userId = UserHolder.getUserId();
        InteractionCheckResponse result = communityService.checkInteractionStatus(userId, targetId, targetType);
        return R.ok(result);
    }
}
