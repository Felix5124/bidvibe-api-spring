package com.bidvibe.bidvibeapispring.dto.bid;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/auctions/sealed-submit.
 * Gửi giá kín trong Sealed-bid Auction.
 */
@Getter
@Setter
@NoArgsConstructor
public class SealedBidRequest {

    @NotNull(message = "ID phiên đấu giá không được để trống")
    private UUID auctionId;

    @NotNull(message = "Giá thầu bí mật không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá thầu phải lớn hơn 0")
    private BigDecimal amount;
}
