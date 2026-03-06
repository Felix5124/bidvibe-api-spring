package com.bidvibe.bidvibeapispring.controller.admin;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.message.MessageResponse;
import com.bidvibe.bidvibeapispring.service.MarketListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * GET /api/admin/market/listings/{id}/messages – lịch sử tin nhắn thương lượng (admin)
 */
@RestController
@RequestMapping("/api/admin/market/listings")
@RequiredArgsConstructor
public class AdminMarketController {

    private final MarketListingService marketListingService;

    // GET /api/admin/market/listings/{id}/messages
    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getDisputeMessages(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(marketListingService.adminGetListingMessages(id)));
    }
}
