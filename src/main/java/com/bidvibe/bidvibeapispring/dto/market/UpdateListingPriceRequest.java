package com.bidvibe.bidvibeapispring.dto.market;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request body cập nhật giá niêm yết Chợ Đen.
 * PATCH /api/market/listings/{id}/price
 */
@Getter
@NoArgsConstructor
public class UpdateListingPriceRequest {

    @NotNull(message = "Giá niêm yết không được để trống")
    @Positive(message = "Giá niêm yết phải lớn hơn 0")
    private BigDecimal askingPrice;
}
