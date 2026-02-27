package com.bidvibe.bidvibeapispring.dto.wallet;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/wallet/deposit.
 */
@Getter
@Setter
@NoArgsConstructor
public class DepositRequest {

    @NotNull(message = "Số tiền nạp không được để trống")
    @DecimalMin(value = "1000", message = "Số tiền nạp tối thiểu là 1.000 VND")
    private BigDecimal amount;

    /** Nội dung chuyển khoản để Admin đối chiếu. */
    private String note;
}
