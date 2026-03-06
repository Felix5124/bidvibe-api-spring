package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.message.SendMessageRequest;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.MarketListingService;
import com.bidvibe.bidvibeapispring.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * Xử lý các message CLIENT → SERVER qua STOMP WebSocket.
 *
 * <p>Client gửi lên: {@code /app/auction/{auctionId}/chat} hoặc {@code /app/market/{listingId}/chat}
 */
@Controller
@RequiredArgsConstructor
public class WsController {

    private final MessageService messageService;
    private final MarketListingService marketListingService;

    /**
     * Chat live trong phòng đấu giá.
     * Client gửi: /app/auction/{auctionId}/chat
     * Server pub: /topic/auction/{auctionId}/chat
     */
    @MessageMapping("/auction/{auctionId}/chat")
    public void auctionChat(
            @DestinationVariable UUID auctionId,
            SimpMessageHeaderAccessor headerAccessor,
            @Payload SendMessageRequest req) {

        UUID senderId = extractUserId(headerAccessor);
        req.setAuctionId(auctionId);
        messageService.sendMessage(senderId, req);
    }

    /**
     * Chat thương lượng trong listing chợ đen.
     * Client gửi: /app/market/{listingId}/chat
     * Server pub: /user/{userId}/queue/messages
     */
    @MessageMapping("/market/{listingId}/chat")
    public void marketChat(
            @DestinationVariable UUID listingId,
            SimpMessageHeaderAccessor headerAccessor,
            @Payload SendMessageRequest req) {

        UUID senderId = extractUserId(headerAccessor);
        marketListingService.sendListingMessage(senderId, listingId, req.getContent());
    }

    // ------------------------------------------------------------------

    private UUID extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        var auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        User user = (User) auth.getPrincipal();
        return user.getId();
    }
}
