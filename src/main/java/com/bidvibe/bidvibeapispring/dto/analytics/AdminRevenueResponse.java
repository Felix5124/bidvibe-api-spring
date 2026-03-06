package com.bidvibe.bidvibeapispring.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * GET /api/admin/analytics/revenue?from=&to=
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRevenueResponse {

    private BigDecimal totalRevenue;
    private List<RevenuePoint> dailyRevenue;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenuePoint {
        private LocalDate date;
        private BigDecimal revenue;
        private long transactionCount;
    }
}
