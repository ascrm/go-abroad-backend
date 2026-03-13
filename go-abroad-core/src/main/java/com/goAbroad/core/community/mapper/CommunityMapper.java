package com.goAbroad.core.community.mapper;

import com.goAbroad.core.community.dto.*;
import com.goAbroad.core.community.entity.Article;
import com.goAbroad.core.community.entity.Answer;
import com.goAbroad.core.community.entity.Question;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommunityMapper {

    CommunityMapper INSTANCE = Mappers.getMapper(CommunityMapper.class);

    ArticleResponse toArticleResponse(Article article);

    List<ArticleResponse> toArticleResponseList(List<Article> articles);

    ArticleResponse toArticleResponseWithUser(Article article, Long userId);

    QuestionResponse toQuestionResponse(Question question);

    List<QuestionResponse> toQuestionResponseList(List<Question> questions);

    QuestionResponse toQuestionResponseWithUser(Question question, Long userId);

    AnswerResponse toAnswerResponse(Answer answer);

    List<AnswerResponse> toAnswerResponseList(List<Answer> answers);

    AnswerResponse toAnswerResponseWithUser(Answer answer, Long userId);

    @Mapping(target = "views", constant = "0")
    @Mapping(target = "favorites", constant = "0")
    Article toEntity(ArticleCreateRequest request);

    void updateFromRequest(ArticleUpdateRequest request, @MappingTarget Article article);

    @Mapping(target = "views", constant = "0")
    @Mapping(target = "repliesCount", constant = "0")
    @Mapping(target = "isResolved", constant = "false")
    @Mapping(target = "isDeleted", constant = "false")
    Question toEntity(QuestionCreateRequest request);

    void updateFromRequest(QuestionUpdateRequest request, @MappingTarget Question question);

    @Mapping(target = "questionId", source = "questionId")
    @Mapping(target = "likes", constant = "0")
    @Mapping(target = "repliesCount", constant = "0")
    @Mapping(target = "isOfficial", constant = "false")
    @Mapping(target = "isBestAnswer", constant = "false")
    @Mapping(target = "isDeleted", constant = "false")
    Answer toEntity(AnswerCreateRequest request);

    void updateFromRequest(AnswerUpdateRequest request, @MappingTarget Answer answer);
}
