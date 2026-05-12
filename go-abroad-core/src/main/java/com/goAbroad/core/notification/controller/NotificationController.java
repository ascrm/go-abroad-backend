package com.goAbroad.core.notification.controller;

import com.goAbroad.common.result.PageR;
import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import com.goAbroad.core.notification.dto.NotificationResponse;
import com.goAbroad.core.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public R<PageR<NotificationResponse>> getNotificationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserHolder.getUserId();
        PageR<NotificationResponse> result = notificationService.getNotificationList(userId, page, pageSize);
        return R.ok(result);
    }

    @GetMapping("/unread-count")
    public R<Map<String, Long>> getUnreadCount() {
        Long userId = UserHolder.getUserId();
        long count = notificationService.getUnreadCount(userId);
        return R.ok(Map.of("count", count));
    }

    @PutMapping("/read-all")
    public R<Void> markAllAsRead() {
        Long userId = UserHolder.getUserId();
        notificationService.markAllAsRead(userId);
        return R.ok();
    }

    @PutMapping("/read/{id}")
    public R<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        notificationService.markAsRead(userId, id);
        return R.ok();
    }

    @PutMapping("/pin/{id}")
    public R<Void> togglePin(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        notificationService.togglePin(userId, id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteNotification(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        notificationService.deleteNotification(userId, id);
        return R.ok();
    }
}