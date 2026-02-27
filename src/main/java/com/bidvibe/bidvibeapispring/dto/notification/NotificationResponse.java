package com.bidvibe.bidvibeapispring.dto.notification;

import com.bidvibe.bidvibeapispring.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response trả về một thông báo hệ thống.
 * Dùng khi lấy danh sách thông báo trong trang cá nhân.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private String title;
    private String content;
    private boolean read;
    private Instant createdAt;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
