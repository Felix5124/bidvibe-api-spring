package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.transaction.ApproveTransactionRequest;
import com.bidvibe.bidvibeapispring.dto.transaction.TransactionResponse;
import com.bidvibe.bidvibeapispring.entity.Transaction;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin panel – duyệt / từ chối các lệnh Nạp và Rút tiền.
 * GET /api/admin/finance/approve  →  danh sách PENDING
 * POST /api/admin/finance/approve →  duyệt / từ chối theo newStatus
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    // ------------------------------------------------------------------
    // Admin – list pending
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TransactionResponse> listPending() {
        return transactionRepository
                .findByTypeInAndStatusOrderByTimestampAsc(
                        List.of(Transaction.Type.DEPOSIT, Transaction.Type.WITHDRAW),
                        Transaction.Status.PENDING)
                .stream().map(TransactionResponse::from).toList();
    }

    // ------------------------------------------------------------------
    // Admin – approve / reject
    // ------------------------------------------------------------------

    /**
     * POST /api/admin/finance/approve
     * newStatus = COMPLETED → duyệt; CANCELLED → từ chối/hoàn tiền
     */
    @Transactional
    public TransactionResponse processTransaction(ApproveTransactionRequest req) {
        Transaction tx = transactionRepository.findById(req.getTransactionId())
                .orElseThrow(() -> new BidVibeException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (tx.getStatus() != Transaction.Status.PENDING) {
            throw new BidVibeException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
        }

        return switch (tx.getType()) {
            case DEPOSIT -> req.getNewStatus() == Transaction.Status.COMPLETED
                    ? walletService.approveDeposit(tx.getId())
                    : cancelDeposit(tx);
            case WITHDRAW -> req.getNewStatus() == Transaction.Status.COMPLETED
                    ? walletService.approveWithdraw(tx.getId())
                    : walletService.rejectWithdraw(tx.getId());
            default -> throw new BidVibeException(ErrorCode.TRANSACTION_NOT_FOUND,
                    "Chỉ duyệt/từ chối DEPOSIT và WITHDRAW");
        };
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    /** Huỷ Deposit PENDING – không cộng tiền. */
    private TransactionResponse cancelDeposit(Transaction tx) {
        tx.setStatus(Transaction.Status.CANCELLED);
        return TransactionResponse.from(transactionRepository.save(tx));
    }
}
