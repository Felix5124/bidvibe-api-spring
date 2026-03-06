package com.bidvibe.bidvibeapispring.dto.market;

import com.bidvibe.bidvibeapispring.dto.item.ItemResponse;
import com.bidvibe.bidvibeapispring.dto.user.UserSummary;
import com.bidvibe.bidvibeapispring.entity.MarketListing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response trả về thông tin một niêm yết trên Chợ Đen.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketListingResponse {

    private UUID id;
    private ItemResponse item;
    private UserSummary seller;
    private BigDecimal askingPrice;
    private UserSummary buyer;
    private MarketListing.Status status;
    private Instant createdAt;
    private Instant updatedAt;

    public static MarketListingResponse from(MarketListing listing) {
        return MarketListingResponse.builder()
                .id(listing.getId())
                .item(ItemResponse.from(listing.getItem()))
                .seller(UserSummary.builder()
                        .id(listing.getSeller().getId())
                        .nickname(listing.getSeller().getNickname())
                        .avatarUrl(listing.getSeller().getAvatarUrl())
                        .reputationScore(listing.getSeller().getReputationScore())
                        .build())
                .askingPrice(listing.getAskingPrice())
                .buyer(listing.getBuyer() == null ? null : UserSummary.builder()
                        .id(listing.getBuyer().getId())
                        .nickname(listing.getBuyer().getNickname())
                        .avatarUrl(listing.getBuyer().getAvatarUrl())
                        .reputationScore(listing.getBuyer().getReputationScore())
                        .build())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }
}
