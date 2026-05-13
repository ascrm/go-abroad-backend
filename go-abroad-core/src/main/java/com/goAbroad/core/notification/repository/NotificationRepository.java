package com.goAbroad.core.notification.repository;

import com.goAbroad.core.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 获取用户的通知列表（按置顶和创建时间排序）
     */
    Page<Notification> findByUserIdOrderByIsPinnedDescCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 获取用户非系统通知列表（按创建时间倒序）
     */
    Page<Notification> findByUserIdAndTypeNotOrderByCreatedAtDesc(Long userId, String type, Pageable pageable);

    /**
     * 获取用户系统通知列表（按创建时间倒序）
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type, Pageable pageable);

    /**
     * 获取用户未读通知数量
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 批量标记为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 标记单条为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 切换置顶状态
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isPinned = :isPinned, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.userId = :userId")
    int updatePinnedStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("isPinned") Boolean isPinned);

    /**
     * 删除通知
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.userId = :userId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}