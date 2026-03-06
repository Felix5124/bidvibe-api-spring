package com.bidvibe.bidvibeapispring.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * GET /api/admin/analytics/overview
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewResponse {

    private long totalUsers;
    private long totalItems;
    private long totalAuctions;
    private long activeAuctions;
    private long totalMarketListings;
    private long activeMarketListings;
    private BigDecimal totalRevenue;
    private long pendingDeposits;
    private long pendingWithdrawals;
}
