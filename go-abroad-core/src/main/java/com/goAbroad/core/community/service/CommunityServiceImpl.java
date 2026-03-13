package com.goAbroad.core.community.service;

import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.common.result.PageR;
import com.goAbroad.core.community.dto.*;
import com.goAbroad.core.community.entity.Article;
import com.goAbroad.core.community.entity.Answer;
import com.goAbroad.core.community.entity.Interaction;
import com.goAbroad.core.community.entity.Question;
import com.goAbroad.core.community.mapper.CommunityMapper;
import com.goAbroad.core.community.repository.AnswerRepository;
import com.goAbroad.core.community.repository.ArticleRepository;
import com.goAbroad.core.community.repository.InteractionRepository;
import com.goAbroad.core.community.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl {

    private final ArticleRepository articleRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final InteractionRepository interactionRepository;

    private final CommunityMapper communityMapper;

    public PageR<ArticleResponse> getArticleList(String tag, Boolean isFeatured, Integer page, Integer pageSize) {
        Page<Article> articlePage = articleRepository.findByCondition(tag, isFeatured, PageRequest.of(page - 1, pageSize));
        List<Article> articles = articlePage.getContent();

        List<ArticleResponse> list = articles.stream()
                .map(communityMapper::toArticleResponse)
                .collect(Collectors.toList());

        return PageR.ok(articlePage.getTotalElements(), list, page, pageSize);
    }

    public ArticleResponse getArticleDetail(Long userId, Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        return toArticleResponse(article, userId);
    }

    @Transactional
    public ArticleResponse createArticle(Long userId, ArticleCreateRequest request) {
        Article article = communityMapper.toEntity(request);
        article.setAuthorId(userId);

        if (Boolean.TRUE.equals(request.getIsPublished())) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleRepository.save(article);
        return toArticleResponse(article, userId);
    }

    @Transactional
    public ArticleResponse updateArticle(Long userId, Long id, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getAuthorId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        communityMapper.updateFromRequest(request, article);

        if (request.getIsPublished() != null && request.getIsPublished() && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleRepository.save(article);
        return toArticleResponse(article, userId);
    }

    @Transactional
    public void deleteArticle(Long userId, Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getAuthorId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        articleRepository.deleteById(id);
    }

    public PageR<QuestionResponse> getQuestionList(String category, Boolean isResolved, Integer page, Integer pageSize) {
        Page<Question> questionPage = questionRepository.findByCondition(category, isResolved, PageRequest.of(page - 1, pageSize));
        List<Question> questions = questionPage.getContent();

        List<QuestionResponse> list = questions.stream()
                .map(communityMapper::toQuestionResponse)
                .collect(Collectors.toList());

        return PageR.ok(questionPage.getTotalElements(), list, page, pageSize);
    }

    public QuestionResponse getQuestionDetail(Long userId, Long id) {
        Question question = questionRepository.findById(id).orElse(null);
        if (question == null || Boolean.TRUE.equals(question.getIsDeleted())) {
            throw new BusinessException("问题不存在");
        }
        return toQuestionResponse(question, userId);
    }

    @Transactional
    public QuestionResponse createQuestion(Long userId, QuestionCreateRequest request) {
        Question question = communityMapper.toEntity(request);
        question.setAuthorId(userId);

        questionRepository.save(question);
        return toQuestionResponse(question, userId);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long userId, Long id, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(id).orElse(null);
        if (question == null || Boolean.TRUE.equals(question.getIsDeleted())) {
            throw new BusinessException("问题不存在");
        }
        if (!question.getAuthorId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        communityMapper.updateFromRequest(request, question);

        questionRepository.save(question);
        return toQuestionResponse(question, userId);
    }

    @Transactional
    public void deleteQuestion(Long userId, Long id) {
        Question question = questionRepository.findById(id).orElse(null);
        if (question == null) {
            throw new BusinessException("问题不存在");
        }
        if (!question.getAuthorId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        question.setIsDeleted(true);
        questionRepository.save(question);
    }

    public PageR<AnswerResponse> getAnswerList(Long questionId, Integer page, Integer pageSize) {
        List<Answer> answers = answerRepository.findByQuestionIdAndIsDeletedFalse(questionId);

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, answers.size());
        List<Answer> pagedList = start < answers.size() ? answers.subList(start, end) : new java.util.ArrayList<>();

        List<AnswerResponse> list = pagedList.stream()
                .map(communityMapper::toAnswerResponse)
                .collect(Collectors.toList());

        return PageR.ok((long) answers.size(), list, page, pageSize);
    }

    public AnswerResponse getAnswerDetail(Long userId, Long id) {
        Answer answer = answerRepository.findById(id).orElse(null);
        if (answer == null || Boolean.TRUE.equals(answer.getIsDeleted())) {
            throw new BusinessException("回答不存在");
        }
        return toAnswerResponse(answer, userId);
    }

    @Transactional
    public AnswerResponse createAnswer(Long userId, AnswerCreateRequest request) {
        Question question = questionRepository.findById(request.getQuestionId()).orElse(null);
        if (question == null || Boolean.TRUE.equals(question.getIsDeleted())) {
            throw new BusinessException("问题不存在");
        }

        Answer answer = communityMapper.toEntity(request);
        answer.setAuthorId(userId);

        answerRepository.save(answer);

        question.setRepliesCount(question.getRepliesCount() + 1);
        questionRepository.save(question);

        return toAnswerResponse(answer, userId);
    }

    @Transactional
    public AnswerResponse updateAnswer(Long userId, Long id, AnswerUpdateRequest request) {
        Answer answer = answerRepository.findById(id).orElse(null);
        if (answer == null || Boolean.TRUE.equals(answer.getIsDeleted())) {
            throw new BusinessException("回答不存在");
        }
        if (!answer.getAuthorId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        communityMapper.updateFromRequest(request, answer);

        answerRepository.save(answer);
        return toAnswerResponse(answer, userId);
    }

    @Transactional
    public void deleteAnswer(Long userId, Long id) {
        Answer answer = answerRepository.findById(id).orElse(null);
        if (answer == null) {
            throw new BusinessException("回答不存在");
        }
        if (!answer.getAuthorId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        answer.setIsDeleted(true);
        answerRepository.save(answer);

        Question question = questionRepository.findById(answer.getQuestionId()).orElse(null);
        if (question != null && question.getRepliesCount() > 0) {
            question.setRepliesCount(question.getRepliesCount() - 1);
            questionRepository.save(question);
        }
    }

    @Transactional
    public InteractionResponse handleFavorite(Long userId, InteractionRequest request) {
        Interaction.TargetType targetType = Interaction.TargetType.valueOf(request.getTargetType());
        Optional<Interaction> existing = interactionRepository.findOne(userId, request.getTargetId(), targetType, Interaction.Action.favorite);

        if (existing.isPresent()) {
            interactionRepository.deleteByUserIdAndTargetIdAndTargetTypeAndAction(userId, request.getTargetId(), targetType, Interaction.Action.favorite);
            return InteractionResponse.builder()
                    .success(true)
                    .action("favorite")
                    .isActive(false)
                    .build();
        }

        Interaction interaction = Interaction.builder()
                .userId(userId)
                .targetId(request.getTargetId())
                .targetType(Interaction.TargetType.valueOf(request.getTargetType()))
                .action(Interaction.Action.favorite)
                .build();
        interactionRepository.save(interaction);

        return InteractionResponse.builder()
                .success(true)
                .action("favorite")
                .isActive(true)
                .build();
    }

    @Transactional
    public InteractionResponse handleLike(Long userId, InteractionRequest request) {
        Interaction.TargetType targetType = Interaction.TargetType.valueOf(request.getTargetType());
        Optional<Interaction> existing = interactionRepository.findOne(userId, request.getTargetId(), targetType, Interaction.Action.like);

        if (existing.isPresent()) {
            interactionRepository.deleteByUserIdAndTargetIdAndTargetTypeAndAction(userId, request.getTargetId(), targetType, Interaction.Action.like);
            return InteractionResponse.builder()
                    .success(true)
                    .action("like")
                    .isActive(false)
                    .build();
        }

        Interaction interaction = Interaction.builder()
                .userId(userId)
                .targetId(request.getTargetId())
                .targetType(Interaction.TargetType.valueOf(request.getTargetType()))
                .action(Interaction.Action.like)
                .build();
        interactionRepository.save(interaction);

        return InteractionResponse.builder()
                .success(true)
                .action("like")
                .isActive(true)
                .build();
    }

    @Transactional
    public InteractionResponse handleFollow(Long userId, InteractionRequest request) {
        Interaction.TargetType targetType = Interaction.TargetType.valueOf(request.getTargetType());
        Optional<Interaction> existing = interactionRepository.findOne(userId, request.getTargetId(), targetType, Interaction.Action.follow);

        if (existing.isPresent()) {
            interactionRepository.deleteByUserIdAndTargetIdAndTargetTypeAndAction(userId, request.getTargetId(), targetType, Interaction.Action.follow);
            return InteractionResponse.builder()
                    .success(true)
                    .action("follow")
                    .isActive(false)
                    .build();
        }

        Interaction interaction = Interaction.builder()
                .userId(userId)
                .targetId(request.getTargetId())
                .targetType(Interaction.TargetType.valueOf(request.getTargetType()))
                .action(Interaction.Action.follow)
                .build();
        interactionRepository.save(interaction);

        return InteractionResponse.builder()
                .success(true)
                .action("follow")
                .isActive(true)
                .build();
    }

    @Transactional
    public void handleView(Long userId, InteractionRequest request) {
        String targetType = request.getTargetType();
        Long targetId = request.getTargetId();

        switch (targetType) {
            case "article":
                Article article = articleRepository.findById(targetId).orElse(null);
                if (article != null) {
                    article.setViews(article.getViews() + 1);
                    articleRepository.save(article);
                }
                break;
            case "question":
                Question question = questionRepository.findById(targetId).orElse(null);
                if (question != null) {
                    question.setViews(question.getViews() + 1);
                    questionRepository.save(question);
                }
                break;
            case "answer":
                Answer answer = answerRepository.findById(targetId).orElse(null);
                if (answer != null) {
                    answer.setLikes(answer.getLikes() + 1);
                    answerRepository.save(answer);
                }
                break;
        }

        Interaction interaction = Interaction.builder()
                .userId(userId)
                .targetId(targetId)
                .targetType(Interaction.TargetType.valueOf(targetType))
                .action(Interaction.Action.view)
                .build();
        interactionRepository.save(interaction);
    }

    public InteractionCheckResponse checkInteractionStatus(Long userId, Long targetId, String targetType) {
        Interaction.TargetType type = Interaction.TargetType.valueOf(targetType);
        List<Interaction> interactions = interactionRepository.findByTarget(userId, targetId, type);

        boolean isFavorited = interactions.stream()
                .anyMatch(i -> i.getAction() == Interaction.Action.favorite);
        boolean isLiked = interactions.stream()
                .anyMatch(i -> i.getAction() == Interaction.Action.like);
        boolean isFollowed = interactions.stream()
                .anyMatch(i -> i.getAction() == Interaction.Action.follow);

        return InteractionCheckResponse.builder()
                .isFavorited(isFavorited)
                .isLiked(isLiked)
                .isFollowed(isFollowed)
                .build();
    }

    private ArticleResponse toArticleResponse(Article article, Long userId) {
        ArticleResponse response = communityMapper.toArticleResponse(article);
        if (userId != null) {
            response.setIsFavorited(checkIsFavorited(userId, article.getId(), "article"));
            response.setIsLiked(checkIsLiked(userId, article.getId(), "article"));
        }
        return response;
    }

    private QuestionResponse toQuestionResponse(Question question, Long userId) {
        QuestionResponse response = communityMapper.toQuestionResponse(question);
        if (userId != null) {
            response.setIsFavorited(checkIsFavorited(userId, question.getId(), "question"));
        }
        return response;
    }

    private AnswerResponse toAnswerResponse(Answer answer, Long userId) {
        AnswerResponse response = communityMapper.toAnswerResponse(answer);
        if (userId != null) {
            response.setIsLiked(checkIsLiked(userId, answer.getId(), "answer"));
        }
        return response;
    }

    private Boolean checkIsFavorited(Long userId, Long targetId, String targetType) {
        Interaction.TargetType type = Interaction.TargetType.valueOf(targetType);
        Optional<Interaction> interaction = interactionRepository.findOne(userId, targetId, type, Interaction.Action.favorite);
        return interaction.isPresent();
    }

    private Boolean checkIsLiked(Long userId, Long targetId, String targetType) {
        Interaction.TargetType type = Interaction.TargetType.valueOf(targetType);
        Optional<Interaction> interaction = interactionRepository.findOne(userId, targetId, type, Interaction.Action.like);
        return interaction.isPresent();
    }
}
