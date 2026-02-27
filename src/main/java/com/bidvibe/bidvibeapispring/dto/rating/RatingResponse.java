package com.bidvibe.bidvibeapispring.dto.rating;

import com.bidvibe.bidvibeapispring.dto.user.UserSummary;
import com.bidvibe.bidvibeapispring.entity.Rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private UUID auctionId;
    private Integer stars;
    private String comment;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static RatingResponse from(Rating rating) {
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
                .auctionId(rating.getAuction().getId())
                .stars(rating.getStars())
                .comment(rating.getComment())
                .build();
    }
}
