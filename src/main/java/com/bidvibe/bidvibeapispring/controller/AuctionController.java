package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.auction.AuctionResponse;
import com.bidvibe.bidvibeapispring.dto.bid.BidResponse;
import com.bidvibe.bidvibeapispring.dto.bid.BuyNowRequest;
import com.bidvibe.bidvibeapispring.dto.bid.PlaceBidRequest;
import com.bidvibe.bidvibeapispring.dto.bid.SealedBidRequest;
import com.bidvibe.bidvibeapispring.dto.bid.SetProxyBidRequest;
import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.message.MessageResponse;
import com.bidvibe.bidvibeapispring.dto.message.SendMessageRequest;
import com.bidvibe.bidvibeapispring.dto.bid.ProxyBidResponse;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.AuctionService;
import com.bidvibe.bidvibeapispring.service.AuctionSessionService;
import com.bidvibe.bidvibeapispring.service.BidService;
import com.bidvibe.bidvibeapispring.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * GET  /api/auctions/session/{sessionId}    – tất cả auction trong phiên
 * GET  /api/auctions/{id}                   – chi tiết auction
 * GET  /api/auctions/{id}/bids              – lịch sử bid (phân trang)
 * POST /api/auctions/{id}/bids              – đặt giá
 * POST /api/auctions/{id}/buy-now           – mua ngay
 * POST /api/auctions/{id}/sealed-bids       – đặt giá kín
 * POST /api/auctions/{id}/proxy-bids        – cài đặt proxy bid
 * DELETE /api/auctions/{id}/proxy-bids      – xóa proxy bid
 * GET  /api/auctions/{id}/messages          – live chat
 * POST /api/auctions/{id}/messages          – gửi tin nhắn live chat
 */
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final AuctionSessionService auctionSessionService;
    private final BidService bidService;
    private final MessageService messageService;

    // GET /api/auctions/session/{sessionId}
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getSessionAuctions(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(auctionSessionService.getSessionAuctions(sessionId)));
    }

    // GET /api/auctions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuctionResponse>> getAuction(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(auctionService.getAuction(id)));
    }

    // GET /api/auctions/{id}/bids?page=0&size=20
    @GetMapping("/{id}/bids")
    public ResponseEntity<ApiResponse<PageResponse<BidResponse>>> getAuctionBids(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = auctionService.getAuctionBids(id,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // POST /api/auctions/{id}/bids
    @PostMapping("/{id}/bids")
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody PlaceBidRequest req) {
        req.setAuctionId(id);
        var result = bidService.placeBid(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // POST /api/auctions/{id}/buy
    @PostMapping("/{id}/buy")
    public ResponseEntity<ApiResponse<BidResponse>> buyNow(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @RequestBody(required = false) BuyNowRequest req) {
        if (req == null) req = new BuyNowRequest();
        req.setAuctionId(id);
        var result = bidService.buyNow(currentUser.getId(), req);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // POST /api/auctions/{id}/sealed-bid
    @PostMapping("/{id}/sealed-bid")
    public ResponseEntity<ApiResponse<BidResponse>> submitSealedBid(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody SealedBidRequest req) {
        req.setAuctionId(id);
        var result = bidService.submitSealedBid(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // POST /api/auctions/{id}/proxy-bid
    @PostMapping("/{id}/proxy-bid")
    public ResponseEntity<ApiResponse<ProxyBidResponse>> setProxyBid(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody SetProxyBidRequest req) {
        req.setAuctionId(id);
        var result = bidService.setProxyBid(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // DELETE /api/auctions/{id}/proxy-bid
    @DeleteMapping("/{id}/proxy-bid")
    public ResponseEntity<ApiResponse<Void>> cancelProxyBid(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        bidService.cancelProxyBid(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // GET /api/auctions/{id}/messages
    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getLiveChatHistory(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.getLiveChatHistory(id)));
    }

    // POST /api/auctions/{id}/messages
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendLiveChatMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest req) {
        req.setAuctionId(id);
        var result = messageService.sendMessage(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }
}
