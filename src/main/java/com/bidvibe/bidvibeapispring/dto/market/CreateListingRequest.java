package com.bidvibe.bidvibeapispring.dto.market;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body khi đăng bán item lên Chợ Đen.
 * POST /api/market/listings
 */
@Getter
@NoArgsConstructor
public class CreateListingRequest {

    @NotNull(message = "ID vật phẩm không được để trống")
    private UUID itemId;

    @NotNull(message = "Giá niêm yết không được để trống")
    @Positive(message = "Giá niêm yết phải lớn hơn 0")
    private BigDecimal askingPrice;
}
