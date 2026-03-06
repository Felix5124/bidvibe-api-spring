package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.item.ItemResponse;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.WatchlistService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET    /api/users/me/watchlist          – danh sách vật phẩm đang theo dõi (phân trang)
 * POST   /api/users/me/watchlist/{itemId} – thêm / bỏ theo dõi (toggle)
 */
@RestController
@RequestMapping("/api/users/me/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    // GET /api/users/me/watchlist?page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> getWatchlist(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = watchlistService.getWatchlistItems(currentUser.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // POST /api/users/me/watchlist
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> addToWatchlist(
            @AuthenticationPrincipal User currentUser,
            @RequestBody WatchlistAddBody body) {
        boolean added = watchlistService.toggleWatchlist(currentUser.getId(), body.itemId());
        String msg = added ? "Đã thêm vào danh sách theo dõi." : "Đã xóa khỏi danh sách theo dõi.";
        return ResponseEntity.ok(ApiResponse.ok(msg, added));
    }

    // DELETE /api/users/me/watchlist/{itemId}
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID itemId) {
        watchlistService.removeFromWatchlist(currentUser.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    record WatchlistAddBody(@NotNull UUID itemId) {}
}
