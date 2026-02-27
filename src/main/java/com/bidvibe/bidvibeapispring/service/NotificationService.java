package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.notification.NotificationResponse;
import com.bidvibe.bidvibeapispring.dto.ws.NotificationPayload;
import com.bidvibe.bidvibeapispring.entity.Notification;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Xử lý nghiệp vụ Thông báo:
 * - Tạo & push thông báo real-time qua WebSocket
 * - Lấy danh sách, đánh dấu đã đọc
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    /** GET /api/notifications – danh sách thông báo của user */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    /** Đếm số thông báo chưa đọc (dùng cho badge UI). */
    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // ------------------------------------------------------------------
    // Mark read
    // ------------------------------------------------------------------

    /** Đánh dấu tất cả thông báo là đã đọc */
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /** Đánh dấu một thông báo là đã đọc */
    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new BidVibeException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    // ------------------------------------------------------------------
    // Create & push (internal – called by BidService, AuctionService…)
    // ------------------------------------------------------------------

    /**
     * Tạo thông báo, lưu DB, rồi push real-time qua WebSocket tới user.
     */
    @Transactional
    public void sendNotification(User user,
                                 String title,
                                 String content,
                                 NotificationPayload.NotificationType type,
                                 UUID referenceId) {
        // 1. Lưu vào DB
        Notification notification = notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .build());

        // 2. Push qua WebSocket /topic/notification/{userId}
        NotificationPayload payload = NotificationPayload.builder()
                .notificationId(notification.getId())
                .userId(user.getId())
                .type(type)
                .title(title)
                .content(content)
                .createdAt(Instant.now())
                .referenceId(referenceId)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/notification/" + user.getId(), payload);
    }
}
