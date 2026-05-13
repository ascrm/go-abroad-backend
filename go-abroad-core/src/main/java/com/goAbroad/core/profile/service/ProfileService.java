package com.goAbroad.core.profile.service;

import com.goAbroad.auth.repository.UserRepository;
import com.goAbroad.core.community.dto.ArticleResponse;
import com.goAbroad.core.community.dto.AuthorDTO;
import com.goAbroad.core.community.dto.QuestionResponse;
import com.goAbroad.core.community.entity.Article;
import com.goAbroad.core.community.entity.Interaction;
import com.goAbroad.core.community.entity.Question;
import com.goAbroad.core.community.mapper.CommunityMapper;
import com.goAbroad.core.community.repository.ArticleRepository;
import com.goAbroad.core.community.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final com.goAbroad.core.community.repository.InteractionRepository interactionRepository;
    private final ArticleRepository articleRepository;
    private final QuestionRepository questionRepository;
    private final CommunityMapper communityMapper;
    private final UserRepository userRepository;

    /**
     * 获取我创建的文章
     */
    public List<ArticleResponse> getMyArticles(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<Article> articles = articleRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        return articles.stream()
                .map(article -> toArticleResponseWithAuthor(article, userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取我收藏的文章
     */
    public List<ArticleResponse> getMyFavoriteArticles(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<Interaction> favorites = interactionRepository.findByUserIdAndTargetTypeAndActionOrderByCreatedAtDesc(
                userId, Interaction.TargetType.article, Interaction.Action.favorite);

        if (favorites.isEmpty()) {
            return List.of();
        }

        Set<Long> articleIds = favorites.stream()
                .map(Interaction::getTargetId)
                .collect(Collectors.toSet());

        List<Article> articles = articleRepository.findAllById(articleIds);
        Map<Long, Boolean> favoriteMap = favorites.stream()
                .collect(Collectors.toMap(Interaction::getTargetId, i -> true));

        return articles.stream()
                .map(article -> {
                    ArticleResponse response = toArticleResponseWithAuthor(article, userId);
                    response.setIsFavorited(favoriteMap.getOrDefault(article.getId(), false));
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取我浏览过的文章
     */
    public List<ArticleResponse> getMyBrowsedArticles(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<Interaction> browsed = interactionRepository.findByUserIdAndTargetTypeAndActionOrderByCreatedAtDesc(
                userId, Interaction.TargetType.article, Interaction.Action.view);

        if (browsed.isEmpty()) {
            return List.of();
        }

        Set<Long> articleIds = browsed.stream()
                .map(Interaction::getTargetId)
                .collect(Collectors.toSet());

        List<Article> articles = articleRepository.findAllById(articleIds);

        return articles.stream()
                .map(article -> toArticleResponseWithAuthor(article, userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取我浏览过的问答
     */
    public List<QuestionResponse> getMyBrowsedQuestions(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<Interaction> browsed = interactionRepository.findByUserIdAndTargetTypeAndActionOrderByCreatedAtDesc(
                userId, Interaction.TargetType.question, Interaction.Action.view);

        if (browsed.isEmpty()) {
            return List.of();
        }

        Set<Long> questionIds = browsed.stream()
                .map(Interaction::getTargetId)
                .collect(Collectors.toSet());

        List<Question> questions = questionRepository.findAllById(questionIds);

        return questions.stream()
                .map(question -> toQuestionResponseWithAuthor(question, userId))
                .collect(Collectors.toList());
    }

    private ArticleResponse toArticleResponseWithAuthor(Article article, Long userId) {
        ArticleResponse response = communityMapper.toArticleResponse(article);
        if (article.getAuthorId() != null) {
            userRepository.findById(article.getAuthorId()).ifPresent(user -> {
                response.setAuthor(AuthorDTO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .build());
            });
        }
        return response;
    }

    private QuestionResponse toQuestionResponseWithAuthor(Question question, Long userId) {
        QuestionResponse response = communityMapper.toQuestionResponse(question);
        if (question.getAuthorId() != null) {
            userRepository.findById(question.getAuthorId()).ifPresent(user -> {
                response.setAuthor(AuthorDTO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .build());
            });
        }
        return response;
    }
}