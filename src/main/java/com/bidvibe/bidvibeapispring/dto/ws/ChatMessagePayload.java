package com.bidvibe.bidvibeapispring.dto.ws;

import com.bidvibe.bidvibeapispring.constant.WsEvents;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket payload cho event: {@value WsEvents#CHAT_MESSAGE}.
 *
 * <p>Destinations:
 * <ul>
 *   <li>Chat Live: {@code /topic/chat/{auctionId}}</li>
 *   <li>Chat P2P:  {@code /topic/chat/p2p/{userId}}</li>
 * </ul>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePayload {

    /** Tên event. */
    private final String event = WsEvents.CHAT_MESSAGE;

    private UUID messageId;
    private UUID senderId;
    private String senderNickname;
    private String senderAvatarUrl;

    /** Null nếu là Chat Live. */
    private UUID receiverId;

    /** Null nếu là Chat P2P. */
    private UUID auctionId;

    private String content;
    private Instant timestamp;
}
