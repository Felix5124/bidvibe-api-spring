package com.bidvibe.bidvibeapispring.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * GET /api/admin/analytics/auctions
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuctionStatsResponse {

    private long totalAuctions;
    private long endedAuctions;
    private long cancelledAuctions;
    private long activeAuctions;
    private long totalBids;
    private BigDecimal avgWinningPrice;
    private BigDecimal totalVolume;
}
