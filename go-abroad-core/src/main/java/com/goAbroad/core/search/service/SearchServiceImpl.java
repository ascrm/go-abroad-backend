package com.goAbroad.core.search.service;

import com.goAbroad.auth.repository.UserRepository;
import com.goAbroad.core.community.repository.ArticleRepository;
import com.goAbroad.core.community.repository.QuestionRepository;
import com.goAbroad.core.plan.repository.PlanRepository;
import com.goAbroad.core.search.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 搜索服务实现 - 使用 PostgreSQL 全文搜索
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int LIMIT = 10;

    private final ArticleRepository articleRepository;
    private final QuestionRepository questionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Override
    public SearchResultDTO searchAll(String keyword) {
        return SearchResultDTO.builder()
                .articles(searchArticles(keyword))
                .plans(searchPlans(keyword))
                .questions(searchQuestions(keyword))
                .users(searchUsers(keyword))
                .build();
    }

    private List<ArticleSearchDTO> searchArticles(String keyword) {
        return articleRepository.searchByKeyword(keyword, LIMIT)
                .stream()
                .map(article -> ArticleSearchDTO.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .description(article.getDescription())
                        .tag(article.getTag())
                        .time(article.getPublishedAt() != null ? article.getPublishedAt().toLocalDate().toString() : null)
                        .build())
                .toList();
    }

    private List<PlanSearchDTO> searchPlans(String keyword) {
        return planRepository.searchByKeyword(keyword, LIMIT)
                .stream()
                .map(plan -> PlanSearchDTO.builder()
                        .id(plan.getId())
                        .title(plan.getTitle())
                        .description(plan.getDescription())
                        .tag("规划")
                        .build())
                .toList();
    }

    private List<QuestionSearchDTO> searchQuestions(String keyword) {
        return questionRepository.searchByKeyword(keyword, LIMIT)
                .stream()
                .map(question -> QuestionSearchDTO.builder()
                        .id(question.getId())
                        .title(question.getTitle())
                        .category(question.getCategory())
                        .tag("问答")
                        .build())
                .toList();
    }

    private List<UserSearchDTO> searchUsers(String keyword) {
        return userRepository.searchByKeyword(keyword, LIMIT)
                .stream()
                .map(user -> UserSearchDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .build())
                .toList();
    }
}