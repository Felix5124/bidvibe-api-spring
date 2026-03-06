package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.item.ItemResponse;
import com.bidvibe.bidvibeapispring.dto.item.ListItemOnMarketRequest;
import com.bidvibe.bidvibeapispring.dto.item.SubmitItemRequest;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * POST /api/items                        – nộp vật phẩm để kiểm duyệt
 * GET  /api/items/inventory              – kho đồ của người dùng hiện tại (phân trang)
 * GET  /api/items/{id}                   – chi tiết vật phẩm
 * POST /api/items/{id}/confirm-receipt   – xác nhận nhận hàng
 * POST /api/items/list-on-market         – niêm yết lên Chợ Đen
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // POST /api/items
    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> submitItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SubmitItemRequest req) {
        var result = itemService.submitItem(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // GET /api/items/me/inventory?page=0&size=20
    @GetMapping("/me/inventory")
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> getInventory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = itemService.getInventory(currentUser.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // GET /api/items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getItemDetail(id)));
    }

    // PATCH /api/items/{id}/confirm-receipt
    @PatchMapping("/{id}/confirm-receipt")
    public ResponseEntity<ApiResponse<ItemResponse>> confirmReceipt(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.confirmReceipt(currentUser.getId(), id)));
    }

    // POST /api/items/list-on-market
    @PostMapping("/list-on-market")
    public ResponseEntity<ApiResponse<ItemResponse>> listOnMarket(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ListItemOnMarketRequest req) {
        var result = itemService.listOnMarket(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }
}
