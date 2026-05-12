package com.goAbroad.core.notification.mapper;

import com.goAbroad.core.notification.dto.NotificationResponse;
import com.goAbroad.core.notification.entity.Notification;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    NotificationResponse toResponse(Notification notification);

    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "isPinned", constant = "false")
    Notification toEntity(NotificationResponse response);
}