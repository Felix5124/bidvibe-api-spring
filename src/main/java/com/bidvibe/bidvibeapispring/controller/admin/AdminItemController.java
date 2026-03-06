package com.bidvibe.bidvibeapispring.controller.admin;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.item.ItemResponse;
import com.bidvibe.bidvibeapispring.dto.item.RejectItemRequest;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * GET  /api/admin/items                  – danh sách vật phẩm (phân trang, lọc status)
 * GET  /api/admin/items/pending          – danh sách đang chờ duyệt (legacy, không phân trang)
 * GET  /api/admin/items/{id}             – chi tiết vật phẩm
 * POST /api/admin/items/{id}/reject      – từ chối vật phẩm
 */
@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemService itemService;

    // GET /api/admin/items?status=PENDING&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> listItems(
            @RequestParam(required = false) Item.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = itemService.adminListItems(status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // GET /api/admin/items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getItemDetail(id)));
    }

    // POST /api/admin/items/{id}/reject
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ItemResponse>> rejectItem(
            @PathVariable UUID id,
            @Valid @RequestBody RejectItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.rejectItem(id, req.getReason())));
    }

    // POST /api/admin/items/{id}/approve
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ItemResponse>> approveItem(
            @PathVariable UUID id,
            @RequestBody ApproveItemBody req) {
        return ResponseEntity.ok(ApiResponse.ok(
                itemService.approveItem(id, req.rarity(), req.tags())));
    }

    record ApproveItemBody(Item.Rarity rarity, List<String> tags) {}
}
