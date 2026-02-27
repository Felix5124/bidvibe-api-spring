package com.bidvibe.bidvibeapispring.dto.wallet;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/wallet/withdraw.
 */
@Getter
@Setter
@NoArgsConstructor
public class WithdrawRequest {

    @NotNull(message = "Số tiền rút không được để trống")
    @DecimalMin(value = "10000", message = "Số tiền rút tối thiểu là 10.000 VND")
    private BigDecimal amount;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    private String bankAccountName;

    @NotBlank(message = "Số tài khoản ngân hàng không được để trống")
    private String bankAccountNumber;

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;
}
