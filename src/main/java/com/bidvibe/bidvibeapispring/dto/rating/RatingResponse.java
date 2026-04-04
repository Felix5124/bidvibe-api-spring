package com.bidvibe.bidvibeapispring.dto.rating;

import com.bidvibe.bidvibeapispring.dto.user.UserSummary;
import com.bidvibe.bidvibeapispring.entity.Rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response trả về thông tin một đánh giá.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {

    private UUID id;
    private UserSummary fromUser;
    private UserSummary toUser;
    /** Null nếu đánh giá cho MarketListing. */
    private UUID auctionId;
    /** Null nếu đánh giá cho Auction. */
    private UUID marketListingId;
    /** Thông tin sản phẩm được đánh giá. */
    private UUID itemId;
    private String itemName;
    private String itemImageUrl;
    private Integer stars;
    private String comment;
    private Instant createdAt;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static RatingResponse from(Rating rating) {
        // Lấy thông tin item từ auction hoặc marketListing
        UUID itemId = null;
        String itemName = null;
        String itemImageUrl = null;
        
        if (rating.getAuction() != null && rating.getAuction().getItem() != null) {
            var item = rating.getAuction().getItem();
            itemId = item.getId();
            itemName = item.getName();
            itemImageUrl = item.getImageUrls() != null && !item.getImageUrls().isEmpty() 
                    ? item.getImageUrls().get(0) : null;
        } else if (rating.getMarketListing() != null && rating.getMarketListing().getItem() != null) {
            var item = rating.getMarketListing().getItem();
            itemId = item.getId();
            itemName = item.getName();
            itemImageUrl = item.getImageUrls() != null && !item.getImageUrls().isEmpty() 
                    ? item.getImageUrls().get(0) : null;
        }
        
        return RatingResponse.builder()
                .id(rating.getId())
                .fromUser(UserSummary.builder()
                        .id(rating.getFromUser().getId())
                        .nickname(rating.getFromUser().getNickname())
                        .avatarUrl(rating.getFromUser().getAvatarUrl())
                        .reputationScore(rating.getFromUser().getReputationScore())
                        .build())
                .toUser(UserSummary.builder()
                        .id(rating.getToUser().getId())
                        .nickname(rating.getToUser().getNickname())
                        .avatarUrl(rating.getToUser().getAvatarUrl())
                        .reputationScore(rating.getToUser().getReputationScore())
                        .build())
                .auctionId(rating.getAuction() != null ? rating.getAuction().getId() : null)
                .marketListingId(rating.getMarketListing() != null ? rating.getMarketListing().getId() : null)
                .itemId(itemId)
                .itemName(itemName)
                .itemImageUrl(itemImageUrl)
                .stars(rating.getStars())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
