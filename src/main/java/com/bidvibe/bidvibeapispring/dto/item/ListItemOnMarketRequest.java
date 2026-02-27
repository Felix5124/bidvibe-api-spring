package com.bidvibe.bidvibeapispring.dto.item;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/market/list – niêm yết đồ từ Kho lên Chợ Đen.
 */
@Getter
@Setter
@NoArgsConstructor
public class ListItemOnMarketRequest {

    @NotNull(message = "ID vật phẩm không được để trống")
    private UUID itemId;

    @NotNull(message = "Giá rao bán không được để trống")
    @DecimalMin(value = "1000", message = "Giá rao bán tối thiểu là 1.000 VND")
    private BigDecimal askingPrice;
}
