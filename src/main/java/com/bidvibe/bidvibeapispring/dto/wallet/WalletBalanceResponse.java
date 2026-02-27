package com.bidvibe.bidvibeapispring.dto.wallet;

import java.math.BigDecimal;
import java.util.UUID;

import com.bidvibe.bidvibeapispring.entity.Wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response cho GET /api/wallet/balance – trả về số dư hai lớp.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceResponse {

    private UUID walletId;

    /** Số dư có thể dùng để đặt giá. */
    private BigDecimal balanceAvailable;

    /** Số dư đang bị khóa (Escrow) bảo đảm cho lệnh bid hiện tại. */
    private BigDecimal balanceLocked;

    /** Tổng số dư = available + locked. */
    private BigDecimal totalBalance;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static WalletBalanceResponse from(Wallet wallet) {
        return WalletBalanceResponse.builder()
                .walletId(wallet.getId())
                .balanceAvailable(wallet.getBalanceAvailable())
                .balanceLocked(wallet.getBalanceLocked())
                .totalBalance(wallet.getBalanceAvailable().add(wallet.getBalanceLocked()))
                .build();
    }
}
