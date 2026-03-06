package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.analytics.PriceHistoryResponse;
import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET /api/analytics/items/{id}/price-history – lịch sử giá của vật phẩm
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // GET /api/analytics/items/{id}/price-history
    @GetMapping("/items/{id}/price-history")
    public ResponseEntity<ApiResponse<PriceHistoryResponse>> getPriceHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getPriceHistory(id)));
    }
}
