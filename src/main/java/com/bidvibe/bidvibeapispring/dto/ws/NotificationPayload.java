package com.bidvibe.bidvibeapispring.dto.ws;

import com.bidvibe.bidvibeapispring.constant.WsEvents;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket payload cho event: {@value WsEvents#NOTIFICATION}.
 *
 * <p>Destination: {@code /topic/notification/{userId}}
 *
 * <p>Các loại thông báo:
 * <ul>
 *   <li>OUTBID – bị người khác vượt giá</li>
 *   <li>WIN    – thắng thầu</li>
 *   <li>WATCHLIST_START – item trong watchlist sắp bắt đầu</li>
 *   <li>DEPOSIT_APPROVED / WITHDRAW_APPROVED – tài chính</li>
 * </ul>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {

    /** Tên event. */
    private final String event = WsEvents.NOTIFICATION;

    private UUID notificationId;
    private UUID userId;

    /** Loại thông báo – giúp Frontend hiển thị icon/màu sắc phù hợp. */
    private NotificationType type;

    private String title;
    private String content;
    private Instant createdAt;

    /** ID liên quan (auctionId, transactionId…) để client điều hướng. */
    private UUID referenceId;

    public enum NotificationType {
        OUTBID,
        WIN,
        WATCHLIST_START,
        DEPOSIT_APPROVED,
        WITHDRAW_APPROVED,
        SYSTEM
    }
}
