package com.bidvibe.bidvibeapispring.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response cho GET /api/analytics/price/{id}.
 * Dữ liệu dùng để vẽ biểu đồ lịch sử giá dạng line-chart neon.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {

    private UUID itemId;
    private String itemName;

    /** Danh sách các điểm dữ liệu theo thời gian. */
    private List<PricePoint> pricePoints;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricePoint {
        private Instant timestamp;
        private BigDecimal price;
        /** Loại sự kiện tạo ra điểm giá (BID / BUY_NOW / PROXY_BID). */
        private String eventType;
    }
}
