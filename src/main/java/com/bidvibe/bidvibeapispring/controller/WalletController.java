package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.transaction.TransactionResponse;
import com.bidvibe.bidvibeapispring.dto.wallet.DepositRequest;
import com.bidvibe.bidvibeapispring.dto.wallet.WalletBalanceResponse;
import com.bidvibe.bidvibeapispring.dto.wallet.WithdrawRequest;
import com.bidvibe.bidvibeapispring.entity.Transaction;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * GET  /api/wallet                  – số dư ví
 * POST /api/wallet/deposit          – yêu cầu nạp tiền
 * POST /api/wallet/withdraw         – yêu cầu rút tiền
 * GET  /api/wallet/transactions     – lịch sử giao dịch (phân trang, lọc type/status)
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // GET /api/wallet
    @GetMapping
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getBalance(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(walletService.getBalance(currentUser.getId())));
    }

    // POST /api/wallet/deposit
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> requestDeposit(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody DepositRequest req) {
        var result = walletService.requestDeposit(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // POST /api/wallet/withdraw
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> requestWithdraw(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody WithdrawRequest req) {
        var result = walletService.requestWithdraw(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // GET /api/wallet/transactions?type=DEPOSIT&status=PENDING&page=0&size=20
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Transaction.Type type,
            @RequestParam(required = false) Transaction.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = walletService.getHistory(currentUser.getId(), type, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }
}
