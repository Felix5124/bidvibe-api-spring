package com.bidvibe.bidvibeapispring.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * GET /api/admin/analytics/market
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMarketStatsResponse {

    private long totalListings;
    private long soldListings;
    private long cancelledListings;
    private long activeListings;
    private BigDecimal avgSalePrice;
    private BigDecimal totalVolume;
}
