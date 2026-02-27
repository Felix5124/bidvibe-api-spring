package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.dto.message.MessageResponse;
import com.bidvibe.bidvibeapispring.dto.message.SendMessageRequest;
import com.bidvibe.bidvibeapispring.dto.ws.ChatMessagePayload;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.Message;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.bidvibe.bidvibeapispring.constant.ErrorCode.*;

/**
 * Xử lý nghiệp vụ Chat:
 * - Chat Live trong phòng đấu giá (auctionId != null)
 * - Chat P2P thương lượng Chợ Đen (receiverId != null)
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final AuctionRepository auctionRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    // ------------------------------------------------------------------
    // Send
    // ------------------------------------------------------------------

    /**
     * Gửi tin nhắn và push real-time qua WebSocket.
     * POST (hoặc gọi nội bộ từ WS controller)
     */
    @Transactional
    public MessageResponse sendMessage(UUID senderId, SendMessageRequest req) {
        if (req.getAuctionId() == null && req.getReceiverId() == null) {
            throw new IllegalArgumentException("Phải có auctionId hoặc receiverId");
        }

        User sender = userService.findById(senderId);
        User receiver = null;
        Auction auction = null;

        if (req.getReceiverId() != null) {
            receiver = userService.findById(req.getReceiverId());
        }
        if (req.getAuctionId() != null) {
            auction = auctionRepository.findById(req.getAuctionId())
                    .orElseThrow(() -> new BidVibeException(AUCTION_NOT_FOUND));
        }

        Message message = messageRepository.save(Message.builder()
                .sender(sender)
                .receiver(receiver)
                .auction(auction)
                .content(req.getContent())
                .build());

        MessageResponse response = MessageResponse.from(message);
        pushViaWebSocket(response, req.getAuctionId(), req.getReceiverId(), senderId);
        return response;
    }

    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    /** Lịch sử Chat Live trong phòng đấu giá */
    @Transactional(readOnly = true)
    public List<MessageResponse> getLiveChatHistory(UUID auctionId) {
        return messageRepository
                .findByAuctionIdAndReceiverIsNullOrderByTimestampAsc(auctionId)
                .stream().map(MessageResponse::from).toList();
    }

    /** GET /api/market/chat-history – Chat P2P giữa 2 user */
    @Transactional(readOnly = true)
    public List<MessageResponse> getP2PHistory(UUID userId, UUID otherId) {
        return messageRepository.findP2PConversation(userId, otherId)
                .stream().map(MessageResponse::from).toList();
    }

    // ------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------

    private void pushViaWebSocket(MessageResponse msg, UUID auctionId, UUID receiverId, UUID senderId) {
        ChatMessagePayload payload = ChatMessagePayload.builder()
                .messageId(msg.getId())
                .senderId(msg.getSender().getId())
                .senderNickname(msg.getSender().getNickname())
                .senderAvatarUrl(msg.getSender().getAvatarUrl())
                .receiverId(receiverId)
                .auctionId(auctionId)
                .content(msg.getContent())
                .timestamp(msg.getTimestamp())
                .build();

        if (auctionId != null) {
            messagingTemplate.convertAndSend("/topic/chat/" + auctionId, payload);
        } else {
            // Push cho cả người nhận lẫn người gửi P2P
            messagingTemplate.convertAndSend("/topic/chat/p2p/" + receiverId, payload);
            messagingTemplate.convertAndSend("/topic/chat/p2p/" + senderId, payload);
        }
    }
}
