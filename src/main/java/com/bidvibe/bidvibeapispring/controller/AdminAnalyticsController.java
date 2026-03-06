package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.analytics.AdminAuctionStatsResponse;
import com.bidvibe.bidvibeapispring.dto.analytics.AdminMarketStatsResponse;
import com.bidvibe.bidvibeapispring.dto.analytics.AdminOverviewResponse;
import com.bidvibe.bidvibeapispring.dto.analytics.AdminRevenueResponse;
import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * API phân tích thống kê cho Admin.
 * Tất cả endpoint yêu cầu role ADMIN (đã bảo vệ thêm tầng /api/admin/** qua SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    /** GET /api/admin/analytics/overview */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getOverview() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getAdminOverview()));
    }

    /**
     * GET /api/admin/analytics/revenue?from=2024-01-01&to=2024-12-31
     * Mặc định: từ đầu tháng đến hôm nay.
     */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<AdminRevenueResponse>> getRevenue(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate effectiveFrom = (from != null) ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveTo   = (to   != null) ? to   : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getAdminRevenue(effectiveFrom, effectiveTo)));
    }

    /** GET /api/admin/analytics/auctions */
    @GetMapping("/auctions")
    public ResponseEntity<ApiResponse<AdminAuctionStatsResponse>> getAuctionStats() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getAdminAuctionStats()));
    }

    /** GET /api/admin/analytics/market */
    @GetMapping("/market")
    public ResponseEntity<ApiResponse<AdminMarketStatsResponse>> getMarketStats() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getAdminMarketStats()));
    }
}
