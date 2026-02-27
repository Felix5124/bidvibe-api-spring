package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.transaction.TransactionResponse;
import com.bidvibe.bidvibeapispring.dto.wallet.DepositRequest;
import com.bidvibe.bidvibeapispring.dto.wallet.WalletBalanceResponse;
import com.bidvibe.bidvibeapispring.dto.wallet.WithdrawRequest;
import com.bidvibe.bidvibeapispring.entity.Transaction;
import com.bidvibe.bidvibeapispring.entity.Wallet;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.TransactionRepository;
import com.bidvibe.bidvibeapispring.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Xử lý nghiệp vụ Ví tiền & Giao dịch tài chính:
 * - Xem số dư
 * - Tạo yêu cầu Nạp / Rút (PENDING → Admin duyệt)
 * - Khóa / Mở khóa số dư khi đặt / huỷ bid (Escrow)
 * - Thực hiện thanh toán cuối khi thắng thầu
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    // ------------------------------------------------------------------
    // Balance
    // ------------------------------------------------------------------

    /** GET /api/wallet/balance */
    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID userId) {
        Wallet wallet = findWalletByUserId(userId);
        return WalletBalanceResponse.from(wallet);
    }

    // ------------------------------------------------------------------
    // Deposit / Withdraw (PENDING – wait for Admin)
    // ------------------------------------------------------------------

    /** POST /api/wallet/deposit – tạo Transaction PENDING, Admin duyệt later. */
    @Transactional
    public TransactionResponse requestDeposit(UUID userId, DepositRequest req) {
        Wallet wallet = findWalletByUserId(userId);
        Transaction tx = transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .type(Transaction.Type.DEPOSIT)
                .amount(req.getAmount())
                .status(Transaction.Status.PENDING)
                .build());
        return TransactionResponse.from(tx);
    }

    /** POST /api/wallet/withdraw – kiểm tra số dư rồi tạo Transaction PENDING. */
    @Transactional
    public TransactionResponse requestWithdraw(UUID userId, WithdrawRequest req) {
        Wallet wallet = findWalletByUserId(userId);
        ensureSufficientBalance(wallet, req.getAmount());

        // Khoá số tiền rút vào locked để không bị dùng trong khi chờ Admin
        wallet.setBalanceAvailable(wallet.getBalanceAvailable().subtract(req.getAmount()));
        wallet.setBalanceLocked(wallet.getBalanceLocked().add(req.getAmount()));
        walletRepository.save(wallet);

        Transaction tx = transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .type(Transaction.Type.WITHDRAW)
                .amount(req.getAmount())
                .status(Transaction.Status.PENDING)
                .build());
        return TransactionResponse.from(tx);
    }

    // ------------------------------------------------------------------
    // Escrow operations (internal – called by BidService)
    // ------------------------------------------------------------------

    /**
     * Khoá số dư khi user đặt bid.
     * available -= amount; locked += amount
     */
    @Transactional
    public void lockFunds(UUID userId, BigDecimal amount) {
        try {
            Wallet wallet = findWalletByUserId(userId);
            ensureSufficientBalance(wallet, amount);
            wallet.setBalanceAvailable(wallet.getBalanceAvailable().subtract(amount));
            wallet.setBalanceLocked(wallet.getBalanceLocked().add(amount));
            walletRepository.save(wallet);
            saveTransaction(wallet, Transaction.Type.BID_LOCK, amount, Transaction.Status.COMPLETED);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BidVibeException(ErrorCode.WALLET_OPTIMISTIC_LOCK, e);
        }
    }

    /**
     * Mở khoá số dư khi user bị vượt giá (outbid).
     * locked -= amount; available += amount
     */
    @Transactional
    public void unlockFunds(UUID userId, BigDecimal amount) {
        try {
            Wallet wallet = findWalletByUserId(userId);
            wallet.setBalanceLocked(wallet.getBalanceLocked().subtract(amount));
            wallet.setBalanceAvailable(wallet.getBalanceAvailable().add(amount));
            walletRepository.save(wallet);
            saveTransaction(wallet, Transaction.Type.BID_UNLOCK, amount, Transaction.Status.COMPLETED);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BidVibeException(ErrorCode.WALLET_OPTIMISTIC_LOCK, e);
        }
    }

    /**
     * Thanh toán cuối khi thắng thầu.
     * locked -= finalAmount; seller.available += (finalAmount - fee)
     */
    @Transactional
    public void processFinalPayment(UUID buyerUserId, UUID sellerUserId, BigDecimal finalAmount, BigDecimal fee) {
        try {
            // Trừ tiền locked của buyer
            Wallet buyerWallet = findWalletByUserId(buyerUserId);
            buyerWallet.setBalanceLocked(buyerWallet.getBalanceLocked().subtract(finalAmount));
            walletRepository.save(buyerWallet);
            saveTransaction(buyerWallet, Transaction.Type.FINAL_PAYMENT, finalAmount, Transaction.Status.COMPLETED);

            // Cộng tiền (trừ phí) vào ví seller
            BigDecimal sellerReceives = finalAmount.subtract(fee);
            Wallet sellerWallet = findWalletByUserId(sellerUserId);
            sellerWallet.setBalanceAvailable(sellerWallet.getBalanceAvailable().add(sellerReceives));
            walletRepository.save(sellerWallet);
            saveTransaction(sellerWallet, Transaction.Type.FINAL_PAYMENT, sellerReceives, Transaction.Status.COMPLETED);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BidVibeException(ErrorCode.WALLET_OPTIMISTIC_LOCK, e);
        }
    }

    // ------------------------------------------------------------------
    // Admin – approve deposit/withdraw
    // ------------------------------------------------------------------

    /**
     * Admin duyệt Nạp tiền: PENDING → COMPLETED, cộng available.
     */
    @Transactional
    public TransactionResponse approveDeposit(UUID transactionId) {
        Transaction tx = findTransactionById(transactionId);
        validatePending(tx);

        Wallet wallet = tx.getWallet();
        wallet.setBalanceAvailable(wallet.getBalanceAvailable().add(tx.getAmount()));
        walletRepository.save(wallet);

        tx.setStatus(Transaction.Status.COMPLETED);
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    /**
     * Admin duyệt Rút tiền: PENDING → COMPLETED, trừ locked (tiền đã bị lock khi tạo request).
     */
    @Transactional
    public TransactionResponse approveWithdraw(UUID transactionId) {
        Transaction tx = findTransactionById(transactionId);
        validatePending(tx);

        Wallet wallet = tx.getWallet();
        wallet.setBalanceLocked(wallet.getBalanceLocked().subtract(tx.getAmount()));
        walletRepository.save(wallet);

        tx.setStatus(Transaction.Status.COMPLETED);
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    /**
     * Admin từ chối Rút tiền: PENDING → CANCELLED, hoàn lại available.
     */
    @Transactional
    public TransactionResponse rejectWithdraw(UUID transactionId) {
        Transaction tx = findTransactionById(transactionId);
        validatePending(tx);

        Wallet wallet = tx.getWallet();
        wallet.setBalanceLocked(wallet.getBalanceLocked().subtract(tx.getAmount()));
        wallet.setBalanceAvailable(wallet.getBalanceAvailable().add(tx.getAmount()));
        walletRepository.save(wallet);

        tx.setStatus(Transaction.Status.CANCELLED);
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    // ------------------------------------------------------------------
    // Transaction history
    // ------------------------------------------------------------------

    /** GET /api/wallet/history */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getHistory(UUID userId, Pageable pageable) {
        Wallet wallet = findWalletByUserId(userId);
        return transactionRepository
                .findByWalletIdOrderByTimestampDesc(wallet.getId(), pageable)
                .map(TransactionResponse::from);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    public Wallet findWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.WALLET_NOT_FOUND));
    }

    private void ensureSufficientBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalanceAvailable().compareTo(amount) < 0) {
            throw new BidVibeException(ErrorCode.WALLET_INSUFFICIENT);
        }
    }

    private void validatePending(Transaction tx) {
        if (tx.getStatus() != Transaction.Status.PENDING) {
            throw new BidVibeException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
        }
    }

    private Transaction findTransactionById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new BidVibeException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    private void saveTransaction(Wallet wallet, Transaction.Type type, BigDecimal amount, Transaction.Status status) {
        transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .status(status)
                .build());
    }
}
