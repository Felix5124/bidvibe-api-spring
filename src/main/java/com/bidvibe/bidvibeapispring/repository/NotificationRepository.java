package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /** Danh sách thông báo của user, mới nhất trước. */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /** Chỉ lấy thông báo chưa đọc. */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /** Đếm số thông báo chưa đọc – hiển thị badge trên UI. */
    long countByUserIdAndIsReadFalse(UUID userId);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc –
     * dùng khi user mở trang thông báo.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") UUID userId);

    /**
     * Đánh dấu một thông báo là đã đọc.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") UUID id, @Param("userId") UUID userId);
}

