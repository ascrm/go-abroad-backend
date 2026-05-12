package com.goAbroad.core.notification.service;

import com.goAbroad.auth.repository.UserRepository;
import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.common.result.PageR;
import com.goAbroad.core.notification.dto.ActorDTO;
import com.goAbroad.core.notification.dto.NotificationResponse;
import com.goAbroad.core.notification.entity.Notification;
import com.goAbroad.core.notification.mapper.NotificationMapper;
import com.goAbroad.core.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public PageR<NotificationResponse> getNotificationList(Long userId, Integer page, Integer pageSize) {
        Page<Notification> notificationPage = notificationRepository
                .findByUserIdOrderByIsPinnedDescCreatedAtDesc(userId, PageRequest.of(page - 1, pageSize));

        List<NotificationResponse> list = notificationPage.getContent().stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());

        return PageR.ok(notificationPage.getTotalElements(), list, page, pageSize);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void togglePin(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        notificationRepository.updatePinnedStatus(notificationId, userId, !notification.getIsPinned());
    }

    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        notificationRepository.deleteByIdAndUserId(notificationId, userId);
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, String type, String title, String content,
                                  Long relatedId, String relatedType, Long actorId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .actorId(actorId)
                .isRead(false)
                .isPinned(false)
                .build();
        notificationRepository.save(notification);
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        NotificationResponse response = notificationMapper.toResponse(notification);

        // 设置Actor信息（通知发送者）
        if (notification.getActorId() != null) {
            userRepository.findById(notification.getActorId()).ifPresent(user ->
                    response.setActor(ActorDTO.builder()
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .avatar(user.getAvatar())
                            .build()));
        }

        // 计算时间字符串
        response.setTime(getTimeAgo(notification.getCreatedAt()));
        response.setCreatedAt(notification.getCreatedAt());

        return response;
    }

    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        if (hours < 24) return hours + "小时前";
        if (days < 7) return days + "天前";
        if (days < 30) return (days / 7) + "周前";
        return (days / 30) + "个月前";
    }
}