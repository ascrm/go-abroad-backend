package com.goAbroad.core.notification.service;

import com.goAbroad.common.result.PageR;
import com.goAbroad.core.notification.dto.NotificationResponse;

public interface NotificationService {

    /**
     * 获取通知列表
     */
    PageR<NotificationResponse> getNotificationList(Long userId, Integer page, Integer pageSize);

    /**
     * 获取未读通知数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记全部已读
     */
    void markAllAsRead(Long userId);

    /**
     * 标记单条已读
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 切换置顶状态
     */
    void togglePin(Long userId, Long notificationId);

    /**
     * 删除通知
     */
    void deleteNotification(Long userId, Long notificationId);

    /**
     * 发送通知（供其他服务调用）
     */
    void sendNotification(Long userId, String type, String content, Long relatedId, String relatedType, Long actorId);

    /**
     * 获取非系统通知列表
     */
    PageR<NotificationResponse> getNonSystemNotificationList(Long userId, Integer page, Integer pageSize);

    /**
     * 获取系统通知列表
     */
    PageR<NotificationResponse> getSystemNotificationList(Long userId, Integer page, Integer pageSize);
}