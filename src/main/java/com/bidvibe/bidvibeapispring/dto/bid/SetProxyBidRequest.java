package com.bidvibe.bidvibeapispring.dto.bid;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body để đặt / cập nhật hạn mức Proxy Bid cho English Auction.
 */
@Getter
@Setter
@NoArgsConstructor
public class SetProxyBidRequest {

    @NotNull(message = "ID phiên đấu giá không được để trống")
    private UUID auctionId;

    @NotNull(message = "Hạn mức tối đa không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Hạn mức phải lớn hơn 0")
    private BigDecimal maxAmount;
}
