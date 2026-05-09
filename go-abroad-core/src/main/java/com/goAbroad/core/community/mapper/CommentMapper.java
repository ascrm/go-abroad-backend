package com.goAbroad.core.community.mapper;

import com.goAbroad.core.community.dto.CommentResponse;
import com.goAbroad.core.community.entity.Comment;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    CommentResponse toCommentResponse(Comment comment);

    List<CommentResponse> toCommentResponseList(List<Comment> comments);

    @Mapping(target = "likes", constant = "0")
    @Mapping(target = "repliesCount", constant = "0")
    @Mapping(target = "isDeleted", constant = "false")
    Comment toEntity(com.goAbroad.core.community.dto.CommentCreateRequest request);
}