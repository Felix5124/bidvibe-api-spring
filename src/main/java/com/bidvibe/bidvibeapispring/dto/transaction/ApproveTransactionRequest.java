package com.bidvibe.bidvibeapispring.dto.transaction;

import com.bidvibe.bidvibeapispring.entity.Transaction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request body cho Admin duyệt lệnh Nạp hoặc Rút tiền.
 * POST /api/admin/finance/approve
 */
@Getter
@Setter
@NoArgsConstructor
public class ApproveTransactionRequest {

    @NotNull(message = "ID giao dịch không được để trống")
    private UUID transactionId;

    @NotNull(message = "Kết quả duyệt không được để trống")
    private Transaction.Status newStatus;

    /** Ghi chú của Admin khi từ chối (tuỳ chọn). */
    private String adminNote;
}
