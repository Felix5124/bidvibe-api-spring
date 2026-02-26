package com.bidvibe.bidvibeapispring.constant;

/**
 * Tên các WebSocket event mà server phát ra (và client lắng nghe).
 * Theo đúng quy chuẩn trong api.md – Section 6: WEB SOCKET EVENTS.
 *
 * Dùng trong:
 *  - SimpMessagingTemplate.convertAndSend(destination, payload)
 *  - Frontend đăng ký subscribe đúng tên event
 */
public final class WsEvents {

    private WsEvents() {}

    // -------------------------------------------------------------------------
    // SERVER → CLIENT  (server bắn ra, client subscribe)
    // -------------------------------------------------------------------------

    /**
     * Cập nhật giá và người dẫn đầu mới nhất trong phòng đấu giá.
     * Destination: /topic/auction/{auctionId}
     * Payload: AuctionUpdatePayload
     */
    public static final String AUCTION_UPDATE = "auction_update";

    /**
     * Tin nhắn chat trong phòng đấu giá (Live Chat) hoặc P2P.
     * Destination: /topic/chat/{auctionId}  hoặc  /topic/chat/p2p/{userId}
     * Payload: ChatMessagePayload
     */
    public static final String CHAT_MESSAGE = "chat_message";

    /**
     * Đồng bộ đồng hồ đếm ngược cho toàn bộ client trong phòng.
     * Destination: /topic/timer/{auctionId}
     * Payload: TimerTickPayload  { remainingSeconds: int }
     */
    public static final String TIMER_TICK = "timer_tick";

    /**
     * Thông báo hệ thống cho từng user cá nhân:
     * - Bị vượt giá (outbid)
     * - Thắng thầu
     * - Watchlist item sắp bắt đầu
     * Destination: /topic/notification/{userId}
     * Payload: NotificationPayload
     */
    public static final String NOTIFICATION = "notification";

    // -------------------------------------------------------------------------
    // CLIENT → SERVER  (client gửi lên, server xử lý)
    // Tương ứng với @MessageMapping trong Controller
    // -------------------------------------------------------------------------

    /**
     * Client gửi tin nhắn chat live vào phòng đấu giá.
     * Destination: /app/chat/{auctionId}
     */
    public static final String APP_CHAT_SEND = "/app/chat/{auctionId}";

    /**
     * Client gửi tin nhắn P2P cho một user khác.
     * Destination: /app/chat/p2p/{receiverId}
     */
    public static final String APP_CHAT_P2P = "/app/chat/p2p/{receiverId}";

    // -------------------------------------------------------------------------
    // DESTINATION BUILDERS  (tiện ích build destination string)
    // -------------------------------------------------------------------------

    public static String auctionUpdateDest(String auctionId) {
        return "/topic/auction/" + auctionId;
    }

    public static String chatDest(String auctionId) {
        return "/topic/chat/" + auctionId;
    }

    public static String timerDest(String auctionId) {
        return "/topic/timer/" + auctionId;
    }

    public static String notificationDest(String userId) {
        return "/topic/notification/" + userId;
    }
}

