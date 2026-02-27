package com.bidvibe.bidvibeapispring.dto.bid;

import com.bidvibe.bidvibeapispring.dto.user.UserSummary;
import com.bidvibe.bidvibeapispring.entity.Bid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response trả về sau khi đặt/xác nhận một lệnh bid.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {

    private UUID id;
    private UUID auctionId;
    private UserSummary bidder;
    private BigDecimal amount;
    private Instant bidTime;

    /** True nếu đây là bid do hệ thống Proxy thực hiện hộ. */
    private boolean proxy;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static BidResponse from(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .auctionId(bid.getAuction().getId())
                .bidder(UserSummary.builder()
                        .id(bid.getUser().getId())
                        .nickname(bid.getUser().getNickname())
                        .avatarUrl(bid.getUser().getAvatarUrl())
                        .reputationScore(bid.getUser().getReputationScore())
                        .build())
                .amount(bid.getAmount())
                .bidTime(bid.getBidTime())
                .proxy(bid.isProxy())
                .build();
    }
}
