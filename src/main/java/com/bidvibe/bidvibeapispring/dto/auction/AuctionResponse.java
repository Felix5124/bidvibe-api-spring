package com.bidvibe.bidvibeapispring.dto.auction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.bidvibe.bidvibeapispring.dto.item.ItemResponse;
import com.bidvibe.bidvibeapispring.dto.user.UserSummary;
import com.bidvibe.bidvibeapispring.entity.Auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response trả về thông tin chi tiết một cuộc đấu giá (Auction).
 * Dùng cho màn hình phòng đấu giá real-time và danh sách.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResponse {

    private UUID id;
    private AuctionSessionResponse session;
    private ItemResponse item;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal minPrice;
    private BigDecimal stepPrice;
    private BigDecimal decreaseAmount;
    private Integer intervalSeconds;
    private Instant endTime;
    private Integer orderIndex;
    private Auction.Status status;

    /** Người thắng (null khi phiên chưa kết thúc). */
    private UserSummary winner;

    // ------------------------------------------------------------------
    // Mapper helper (lazy fields cần đã được fetch trước)
    // ------------------------------------------------------------------

    public static AuctionResponse from(Auction auction) {
        return AuctionResponse.builder()
                .id(auction.getId())
                .session(AuctionSessionResponse.from(auction.getSession()))
                .item(ItemResponse.from(auction.getItem()))
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .minPrice(auction.getMinPrice())
                .stepPrice(auction.getStepPrice())
                .decreaseAmount(auction.getDecreaseAmount())
                .intervalSeconds(auction.getIntervalSeconds())
                .endTime(auction.getEndTime())
                .orderIndex(auction.getOrderIndex())
                .status(auction.getStatus())
                .winner(auction.getWinner() != null ? UserSummary.builder()
                        .id(auction.getWinner().getId())
                        .nickname(auction.getWinner().getNickname())
                        .avatarUrl(auction.getWinner().getAvatarUrl())
                        .reputationScore(auction.getWinner().getReputationScore())
                        .build() : null)
                .build();
    }
}
