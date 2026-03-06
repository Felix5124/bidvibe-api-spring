package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.market.CreateListingRequest;
import com.bidvibe.bidvibeapispring.dto.market.MarketListingResponse;
import com.bidvibe.bidvibeapispring.dto.message.MessageResponse;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.MarketListingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * GET    /api/market                      – tìm kiếm listing (phân trang)
 * GET    /api/market/{id}                 – chi tiết listing
 * DELETE /api/market/{id}                 – chủ listing huỷ niêm yết
 * POST   /api/market/{id}/buy             – mua ngay
 * GET    /api/market/{id}/messages        – lịch sử tin nhắn thương lượng
 * POST   /api/market/{id}/messages        – gửi tin nhắn thương lượng
 */
@RestController
@RequestMapping("/api/market/listings")
@RequiredArgsConstructor
@Validated
public class MarketController {

    private final MarketListingService marketListingService;

    // GET /api/market/listings?keyword=&rarity=RARE&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MarketListingResponse>>> searchListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Item.Rarity rarity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = marketListingService.searchListings(keyword, rarity,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // POST /api/market/listings
    @PostMapping
    public ResponseEntity<ApiResponse<MarketListingResponse>> createListing(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateListingRequest req) {
        var result = marketListingService.createListing(
                currentUser.getId(), req.getItemId(), req.getAskingPrice());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // GET /api/market/listings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarketListingResponse>> getListingDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(marketListingService.getListingDetail(id)));
    }

    // DELETE /api/market/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelListing(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        marketListingService.cancelListing(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // POST /api/market/{id}/buy
    @PostMapping("/{id}/buy")
    public ResponseEntity<ApiResponse<MarketListingResponse>> buyListing(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        var result = marketListingService.buyListing(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // GET /api/market/{id}/messages
    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(marketListingService.getListingMessages(currentUser.getId(), id)));
    }

    // POST /api/market/{id}/messages
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @RequestBody MessageBody body) {
        var result = marketListingService.sendListingMessage(currentUser.getId(), id, body.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    record MessageBody(@NotBlank @Size(max = 1000) String content) {}
}
