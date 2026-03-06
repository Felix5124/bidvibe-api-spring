package com.bidvibe.bidvibeapispring.controller.admin;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.transaction.ApproveTransactionRequest;
import com.bidvibe.bidvibeapispring.dto.transaction.TransactionResponse;
import com.bidvibe.bidvibeapispring.entity.Transaction;
import com.bidvibe.bidvibeapispring.service.TransactionService;
import com.bidvibe.bidvibeapispring.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET  /api/admin/transactions          – tất cả giao dịch (phân trang, lọc type/status)
 * GET  /api/admin/transactions/pending  – giao dịch đang pending (Deposit + Withdraw)
 * POST /api/admin/transactions/approve  – duyệt / từ chối giao dịch
 * POST /api/admin/transactions/{id}/approve-deposit   – duyệt nạp tiền
 * POST /api/admin/transactions/{id}/reject-deposit    – từ chối nạp tiền
 * POST /api/admin/transactions/{id}/approve-withdraw  – duyệt rút tiền
 * POST /api/admin/transactions/{id}/reject-withdraw   – từ chối rút tiền
 */
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final TransactionService transactionService;
    private final WalletService walletService;

    // GET /api/admin/transactions?type=DEPOSIT&status=PENDING&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> listAll(
            @RequestParam(required = false) Transaction.Type type,
            @RequestParam(required = false) Transaction.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = transactionService.listAllPaged(type, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // GET /api/admin/transactions/pending
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<?>> listPending() {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.listPending()));
    }

    // POST /api/admin/transactions/approve  (legacy bulk endpoint)
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<TransactionResponse>> processTransaction(
            @Valid @RequestBody ApproveTransactionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.processTransaction(req)));
    }

    // POST /api/admin/transactions/{id}/approve-deposit
    @PostMapping("/{id}/approve-deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> approveDeposit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(walletService.approveDeposit(id)));
    }

    // POST /api/admin/transactions/{id}/reject-deposit
    @PostMapping("/{id}/reject-deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> rejectDeposit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(walletService.rejectDeposit(id)));
    }

    // POST /api/admin/transactions/{id}/approve-withdraw
    @PostMapping("/{id}/approve-withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> approveWithdraw(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(walletService.approveWithdraw(id)));
    }

    // POST /api/admin/transactions/{id}/reject-withdraw
    @PostMapping("/{id}/reject-withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> rejectWithdraw(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(walletService.rejectWithdraw(id)));
    }

    // POST /api/admin/transactions/{id}/approve
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TransactionResponse>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.approve(id)));
    }

    // POST /api/admin/transactions/{id}/reject
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<TransactionResponse>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.reject(id)));
    }
}
