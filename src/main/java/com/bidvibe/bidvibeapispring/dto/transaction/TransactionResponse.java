package com.bidvibe.bidvibeapispring.dto.transaction;

import com.bidvibe.bidvibeapispring.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response trả về một bản ghi giao dịch tài chính.
 * Dùng cho GET /api/wallet/history.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;
    private UUID walletId;
    private Transaction.Type type;
    private BigDecimal amount;
    private Transaction.Status status;
    private Instant timestamp;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static TransactionResponse from(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .walletId(tx.getWallet().getId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .timestamp(tx.getTimestamp())
                .build();
    }
}
