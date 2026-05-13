package com.goAbroad.auth.mapper;

import com.goAbroad.auth.dto.UserInfoResponse;
import com.goAbroad.auth.dto.UserInfoUpdateRequest;
import com.goAbroad.auth.entity.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * 用户信息Mapper
 * 使用MapStruct进行实体与DTO之间的转换
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * 实体转响应DTO
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "birthday", expression = "java(user.getBirthday() != null ? user.getBirthday().toString() : null)")
    @Mapping(target = "bgUrl", source = "bgUrl")
    UserInfoResponse toResponse(User user);

    /**
     * 更新请求转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "birthday", expression = "java(request.getBirthday() != null && !request.getBirthday().isEmpty() ? java.time.LocalDate.parse(request.getBirthday()) : null)")
    void updateFromRequest(UserInfoUpdateRequest request, @MappingTarget User user);
}
