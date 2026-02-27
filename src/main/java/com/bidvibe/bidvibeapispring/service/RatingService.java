package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.rating.CreateRatingRequest;
import com.bidvibe.bidvibeapispring.dto.rating.RatingResponse;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.Rating;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
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
    private final UserService userService;

    // ------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------

    /** POST /api/users/rate */
    @Transactional
    public RatingResponse createRating(UUID fromUserId, CreateRatingRequest req) {
        // Check đã rate giao dịch này chưa
        if (ratingRepository.existsByFromUserIdAndAuctionId(fromUserId, req.getAuctionId())) {
            throw new BidVibeException(ErrorCode.RATING_ALREADY_SUBMITTED);
        }

        Auction auction = auctionRepository.findById(req.getAuctionId())
                .orElseThrow(() -> new BidVibeException(ErrorCode.AUCTION_NOT_FOUND));

        // Chỉ cho phép đánh giá nếu auction đã ENDED
        if (auction.getStatus() != Auction.Status.ENDED) {
            throw new BidVibeException(ErrorCode.RATING_NOT_ELIGIBLE);
        }

        User fromUser = userService.findById(fromUserId);
        User toUser = userService.findById(req.getToUserId());

        Rating rating = ratingRepository.save(Rating.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .auction(auction)
                .stars(req.getStars())
                .comment(req.getComment())
                .build());

        // Cập nhật điểm uy tín của người được đánh giá
        userService.refreshReputationScore(req.getToUserId());

        return RatingResponse.from(rating);
    }

    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    /** Lấy tất cả đánh giá nhận được của một user (hiển thị trên profile). */
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByUser(UUID userId) {
        return ratingRepository.findByToUserIdOrderByIdDesc(userId)
                .stream().map(RatingResponse::from).toList();
    }
}
