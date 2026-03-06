package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.transaction.ApproveTransactionRequest;
import com.bidvibe.bidvibeapispring.dto.transaction.TransactionResponse;
import com.bidvibe.bidvibeapispring.entity.Transaction;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    /** Admin – danh sách tất cả giao dịch có phân trang (GET /api/admin/transactions). */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> listAllPaged(Transaction.Type type,
                                                   Transaction.Status status,
                                                   Pageable pageable) {
        return transactionRepository.findAllFiltered(type, status, pageable)
                .map(TransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listPending() {
        return transactionRepository
                .findByTypeInAndStatusOrderByCreatedAtAsc(
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

    /**
     * POST /api/admin/transactions/{id}/approve
     * Tự động phát hiện type và duyệt.
     */
    @Transactional
    public TransactionResponse approve(UUID transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.TRANSACTION_NOT_FOUND));
        return switch (tx.getType()) {
            case DEPOSIT  -> walletService.approveDeposit(transactionId);
            case WITHDRAW -> walletService.approveWithdraw(transactionId);
            default -> throw new BidVibeException(ErrorCode.TRANSACTION_NOT_FOUND,
                    "Chỉ duyệt DEPOSIT và WITHDRAW");
        };
    }

    /**
     * POST /api/admin/transactions/{id}/reject
     * Tự động phát hiện type và từ chối.
     */
    @Transactional
    public TransactionResponse reject(UUID transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.TRANSACTION_NOT_FOUND));
        return switch (tx.getType()) {
            case DEPOSIT  -> walletService.rejectDeposit(transactionId);
            case WITHDRAW -> walletService.rejectWithdraw(transactionId);
            default -> throw new BidVibeException(ErrorCode.TRANSACTION_NOT_FOUND,
                    "Chỉ từ chối DEPOSIT và WITHDRAW");
        };
    }
}
