package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.dto.ws.AuctionUpdatePayload;
import com.bidvibe.bidvibeapispring.dto.ws.ChatMessagePayload;
import com.bidvibe.bidvibeapispring.dto.ws.NotificationPayload;
import com.bidvibe.bidvibeapispring.dto.ws.TimerTickPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Tập trung tất cả lệnh publish WebSocket vào một service duy nhất.
 * Các service khác (AuctionService, MessageService, v.v.) inject class này
 * thay vì inject SimpMessagingTemplate trực tiếp.
 */
@Service
@RequiredArgsConstructor
public class WsEventPublisher {

    private final SimpMessagingTemplate messaging;

    /** Broadcast cập nhật trạng thái đấu giá tới tất cả người trong phòng. */
    public void publishAuctionUpdate(UUID auctionId, AuctionUpdatePayload payload) {
        messaging.convertAndSend("/topic/auction/" + auctionId, payload);
    }

    /** Broadcast đồng hồ đếm ngược mỗi giây. */
    public void publishTimerTick(UUID auctionId, TimerTickPayload payload) {
        messaging.convertAndSend("/topic/timer/" + auctionId, payload);
    }

    /** Gửi thông báo cá nhân tới một user. */
    public void publishNotification(UUID userId, NotificationPayload payload) {
        messaging.convertAndSendToUser(userId.toString(), "/queue/notifications", payload);
    }

    /** Broadcast tin nhắn chat live trong phòng đấu giá. */
    public void publishAuctionChat(UUID auctionId, ChatMessagePayload payload) {
        messaging.convertAndSend("/topic/chat/auction/" + auctionId, payload);
    }

    /** Gửi tin nhắn P2P tới người nhận. */
    public void publishP2pMessage(UUID receiverId, ChatMessagePayload payload) {
        messaging.convertAndSendToUser(receiverId.toString(), "/queue/messages", payload);
    }

    /**
     * Gửi tín hiệu kick tới client để ngắt kết nối khỏi phòng đấu giá.
     * Client lắng nghe /user/queue/kick và tự disconnect.
     */
    public void publishKick(UUID userId, UUID auctionId) {
        messaging.convertAndSendToUser(userId.toString(), "/queue/kick",
                java.util.Map.of("auctionId", auctionId.toString(), "event", "kick"));
    }
}
