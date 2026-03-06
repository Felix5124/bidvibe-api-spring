package com.bidvibe.bidvibeapispring.controller.admin;

import com.bidvibe.bidvibeapispring.dto.auction.ApproveItemRequest;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionResponse;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionSessionResponse;
import com.bidvibe.bidvibeapispring.dto.auction.CreateAuctionSessionRequest;
import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import com.bidvibe.bidvibeapispring.service.AuctionService;
import com.bidvibe.bidvibeapispring.service.AuctionSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * POST   /api/admin/sessions                           – tạo phiên mới
 * GET    /api/admin/sessions                           – danh sách phiên (phân trang, lọc)
 * GET    /api/admin/sessions/{id}                      – chi tiết phiên
 * GET    /api/admin/sessions/{id}/auctions             – danh sách auction trong phiên
 * POST   /api/admin/sessions/{id}/items                – thêm item vào phiên (duyệt item)
 * DELETE /api/admin/sessions/{id}/auctions/{auctionId} – xóa auction khỏi phiên
 * POST   /api/admin/sessions/{id}/activate             – kích hoạt phiên
 * POST   /api/admin/sessions/{id}/pause                – tạm dừng phiên
 * POST   /api/admin/sessions/{id}/resume               – tiếp tục phiên
 * POST   /api/admin/sessions/{id}/stop                 – dừng phiên
 * POST   /api/admin/auctions/{id}/reset-timer          – reset đồng hồ
 */
@RestController
@RequiredArgsConstructor
public class AdminAuctionSessionController {

    private final AuctionSessionService auctionSessionService;
    private final AuctionService auctionService;

    // POST /api/admin/sessions
    @PostMapping("/api/admin/sessions")
    public ResponseEntity<ApiResponse<AuctionSessionResponse>> createSession(
            @Valid @RequestBody CreateAuctionSessionRequest req) {
        var result = auctionSessionService.createSession(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // GET /api/admin/sessions?status=ACTIVE&type=ENGLISH&page=0&size=10
    @GetMapping("/api/admin/sessions")
    public ResponseEntity<ApiResponse<PageResponse<AuctionSessionResponse>>> listSessions(
            @RequestParam(required = false) AuctionSession.Status status,
            @RequestParam(required = false) AuctionSession.Type type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = auctionSessionService.listSessions(status, type,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // GET /api/admin/sessions/{id}
    @GetMapping("/api/admin/sessions/{id}")
    public ResponseEntity<ApiResponse<AuctionSessionResponse>> getSession(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionSessionService.getSession(id)));
    }

    // GET /api/admin/sessions/{id}/auctions
    @GetMapping("/api/admin/sessions/{id}/auctions")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getSessionAuctions(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionSessionService.getSessionAuctions(id)));
    }

    // POST /api/admin/sessions/{id}/auctions  – thêm item + tạo Auction record
    @PostMapping("/api/admin/sessions/{id}/auctions")
    public ResponseEntity<ApiResponse<AuctionResponse>> addItemToSession(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveItemRequest req) {
        req.setSessionId(id);
        var result = auctionSessionService.addItemToSession(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // DELETE /api/admin/sessions/{id}/auctions/{auctionId}
    @DeleteMapping("/api/admin/sessions/{id}/auctions/{auctionId}")
    public ResponseEntity<ApiResponse<Void>> removeItemFromSession(
            @PathVariable UUID id,
            @PathVariable UUID auctionId) {
        auctionSessionService.removeItemFromSession(id, auctionId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // POST /api/admin/sessions/{id}/start
    @PostMapping("/api/admin/sessions/{id}/start")
    public ResponseEntity<ApiResponse<AuctionSessionResponse>> activateSession(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionSessionService.activateSession(id)));
    }

    // POST /api/admin/sessions/{id}/pause
    @PostMapping("/api/admin/sessions/{id}/pause")
    public ResponseEntity<ApiResponse<AuctionSessionResponse>> pauseSession(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionSessionService.pauseSession(id)));
    }

    // POST /api/admin/sessions/{id}/resume
    @PostMapping("/api/admin/sessions/{id}/resume")
    public ResponseEntity<ApiResponse<AuctionSessionResponse>> resumeSession(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionSessionService.resumeSession(id)));
    }

    // POST /api/admin/sessions/{id}/stop
    @PostMapping("/api/admin/sessions/{id}/stop")
    public ResponseEntity<ApiResponse<AuctionSessionResponse>> stopSession(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionSessionService.stopSession(id)));
    }

    // POST /api/admin/auctions/{id}/reset-timer
    @PostMapping("/api/admin/auctions/{id}/reset-timer")
    public ResponseEntity<ApiResponse<AuctionResponse>> resetAuctionTimer(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionService.resetAuctionTimer(id)));
    }

    // DELETE /api/admin/auctions/{auctionId}/bids/{bidId}
    @DeleteMapping("/api/admin/auctions/{auctionId}/bids/{bidId}")
    public ResponseEntity<ApiResponse<Void>> removeBid(
            @PathVariable UUID auctionId,
            @PathVariable UUID bidId) {
        auctionService.removeBid(auctionId, bidId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
