package com.bidvibe.bidvibeapispring.dto.message;

import com.bidvibe.bidvibeapispring.dto.user.UserSummary;
import com.bidvibe.bidvibeapispring.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response trả về một tin nhắn chat (Live hoặc P2P).
 * Endpoint: GET /api/market/chat-history
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private UUID id;
    private UserSummary sender;

    /** Null nếu là Chat Live trong phòng đấu giá. */
    private UserSummary receiver;

    /** UUID phòng đấu giá; null nếu là Chat P2P. */
    private UUID auctionId;

    private String content;
    private Instant timestamp;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .sender(UserSummary.builder()
                        .id(message.getSender().getId())
                        .nickname(message.getSender().getNickname())
                        .avatarUrl(message.getSender().getAvatarUrl())
                        .reputationScore(message.getSender().getReputationScore())
                        .build())
                .receiver(message.getReceiver() != null ? UserSummary.builder()
                        .id(message.getReceiver().getId())
                        .nickname(message.getReceiver().getNickname())
                        .avatarUrl(message.getReceiver().getAvatarUrl())
                        .reputationScore(message.getReceiver().getReputationScore())
                        .build() : null)
                .auctionId(message.getAuction() != null ? message.getAuction().getId() : null)
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}
