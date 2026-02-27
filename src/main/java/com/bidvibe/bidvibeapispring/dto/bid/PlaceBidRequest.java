package com.bidvibe.bidvibeapispring.dto.bid;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/auctions/bid.
 * Dùng cho cả Manual Bid (English) và Dutch Auction buy-now bid.
 */
@Getter
@Setter
@NoArgsConstructor
public class PlaceBidRequest {

    @NotNull(message = "ID phiên đấu giá không được để trống")
    private UUID auctionId;

    @NotNull(message = "Số tiền đặt giá không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;
}
