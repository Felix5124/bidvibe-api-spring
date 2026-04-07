package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.auction.AuctionResponse;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionSessionResponse;
import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import com.bidvibe.bidvibeapispring.service.AuctionSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET /api/sessions        – danh sách phiên (phân trang, lọc status/type)
 * GET /api/sessions/{id}   – chi tiết phiên
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class AuctionSessionController {

    private final AuctionSessionService auctionSessionService;

    // GET /api/sessions?status=ACTIVE&type=ENGLISH&page=0&size=10
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuctionSessionResponse>>> listSessions(
            @RequestParam(required = false) AuctionSession.Status status,
            @RequestParam(required = false) AuctionSession.Type type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AuctionSessionResponse> result;
        if (status == null && type == null) {
            result = auctionSessionService.listPublicLobbySessions(PageRequest.of(page, size));
        } else {
            result = auctionSessionService.listSessions(status, type,
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // GET /api/sessions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuctionSessionResponse>> getSession(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionSessionService.getSession(id)));
    }

    // GET /api/sessions/{id}/auctions?page=0&size=50
    @GetMapping("/{id}/auctions")
    public ResponseEntity<ApiResponse<PageResponse<AuctionResponse>>> getSessionAuctions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var result = auctionSessionService.getSessionAuctions(id,
                PageRequest.of(page, size, Sort.by("orderIndex").ascending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }
}
