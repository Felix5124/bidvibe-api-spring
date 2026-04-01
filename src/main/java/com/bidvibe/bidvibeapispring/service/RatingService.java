package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.rating.CreateRatingRequest;
import com.bidvibe.bidvibeapispring.dto.rating.RatingResponse;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.MarketListing;
import com.bidvibe.bidvibeapispring.entity.Rating;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.repository.MarketListingRepository;
import com.bidvibe.bidvibeapispring.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Xử lý nghiệp vụ Đánh giá (Rating):
 * - Tạo đánh giá sau giao dịch thành công (1 lần mỗi giao dịch)
 * - Lấy danh sách đánh giá của user
 * - Cập nhật điểm uy tín sau khi có rating mới
 */
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final AuctionRepository auctionRepository;
    private final MarketListingRepository marketListingRepository;
    private final UserService userService;

    // ------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------

    /** POST /api/ratings */
    @Transactional
    public RatingResponse createRating(UUID fromUserId, CreateRatingRequest req) {
        User fromUser = userService.findById(fromUserId);

        Rating.RatingBuilder builder = Rating.builder()
                .fromUser(fromUser)
                .stars(req.getStars())
                .comment(req.getComment());

        User toUser = null;

        if (req.getAuctionId() != null) {
            // Path: đánh giá sau đấu giá
            if (ratingRepository.existsByFromUserIdAndAuctionId(fromUserId, req.getAuctionId())) {
                throw new BidVibeException(ErrorCode.RATING_ALREADY_SUBMITTED);
            }
            Auction auction = auctionRepository.findById(req.getAuctionId())
                    .orElseThrow(() -> new BidVibeException(ErrorCode.AUCTION_NOT_FOUND));
            if (auction.getStatus() != Auction.Status.ENDED) {
                throw new BidVibeException(ErrorCode.RATING_NOT_ELIGIBLE);
            }
            builder.auction(auction);

            // Xác định người được đánh giá: nếu fromUser là winner → đánh giá seller, ngược lại
            if (auction.getWinner() != null && auction.getWinner().getId().equals(fromUserId)) {
                toUser = auction.getItem().getSeller();
            } else {
                toUser = auction.getWinner();
                if (toUser == null) {
                    throw new BidVibeException(ErrorCode.RATING_NOT_ELIGIBLE, "Chưa có người thắng để đánh giá");
                }
            }
        } else if (req.getMarketListingId() != null) {
            // Path: đánh giá sau giao dịch Chợ Đen
            if (ratingRepository.existsByFromUserIdAndMarketListingId(fromUserId, req.getMarketListingId())) {
                throw new BidVibeException(ErrorCode.RATING_ALREADY_SUBMITTED);
            }
            MarketListing listing = marketListingRepository.findById(req.getMarketListingId())
                    .orElseThrow(() -> new BidVibeException(ErrorCode.RESOURCE_NOT_FOUND));
            if (listing.getStatus() != MarketListing.Status.SOLD) {
                throw new BidVibeException(ErrorCode.RATING_NOT_ELIGIBLE);
            }
            builder.marketListing(listing);

            // Xác định người được đánh giá: nếu fromUser là seller → đánh giá buyer, ngược lại
            if (listing.getSeller().getId().equals(fromUserId)) {
                toUser = listing.getBuyer();
            } else {
                toUser = listing.getSeller();
            }
            if (toUser == null) {
                throw new BidVibeException(ErrorCode.RATING_NOT_ELIGIBLE, "Chưa có người mua để đánh giá");
            }
        } else {
            throw new BidVibeException(ErrorCode.VALIDATION_FAILED, "Phải cung cấp auctionId hoặc marketListingId");
        }

        builder.toUser(toUser);
        Rating rating = ratingRepository.save(builder.build());

        // Cập nhật điểm uy tín của người được đánh giá
        userService.refreshReputationScore(toUser.getId());

        return RatingResponse.from(rating);
    }

    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    /** Lấy tất cả đánh giá nhận được của một user (hiển thị trên profile). */
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByUser(UUID userId) {
        return ratingRepository.findByToUserIdOrderByCreatedAtDesc(userId)
                .stream().map(RatingResponse::from).toList();
    }
}
