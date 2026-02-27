package com.bidvibe.bidvibeapispring.dto.bid;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/auctions/buy-now.
 * Nhấn "MUA" ngay trong Dutch Auction.
 */
@Getter
@Setter
@NoArgsConstructor
public class BuyNowRequest {

    @NotNull(message = "ID phiên đấu giá không được để trống")
    private UUID auctionId;
}
