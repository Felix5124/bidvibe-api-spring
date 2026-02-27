package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionResponse;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.entity.Watchlist;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Xử lý nghiệp vụ Watchlist:
 * - Thêm / Xóa theo dõi (toggle)
 * - Lấy danh sách item đang theo dõi
 */
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final AuctionRepository auctionRepository;
    private final UserService userService;

    // ------------------------------------------------------------------
    // Toggle
    // ------------------------------------------------------------------

    /**
     * POST /api/users/watchlist/{auctionId} – toggle theo dõi.
     * Nếu chưa theo dõi → thêm, đã theo dõi → xóa.
     * @return true nếu đã thêm, false nếu đã xóa
     */
    @Transactional
    public boolean toggleWatchlist(UUID userId, UUID auctionId) {
        if (watchlistRepository.existsByUserIdAndAuctionId(userId, auctionId)) {
            watchlistRepository.deleteByUserIdAndAuctionId(userId, auctionId);
            return false;
        }

        User user = userService.findById(userId);
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.AUCTION_NOT_FOUND));

        watchlistRepository.save(Watchlist.builder()
                .user(user)
                .auction(auction)
                .build());
        return true;
    }

    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    /** GET /api/users/watchlist */
    @Transactional(readOnly = true)
    public List<AuctionResponse> getWatchlist(UUID userId) {
        return watchlistRepository.findByUserId(userId)
                .stream()
                .map(w -> AuctionResponse.from(w.getAuction()))
                .toList();
    }
}
