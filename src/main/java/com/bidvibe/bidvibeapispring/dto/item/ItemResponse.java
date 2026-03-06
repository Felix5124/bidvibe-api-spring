package com.bidvibe.bidvibeapispring.dto.item;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bidvibe.bidvibeapispring.dto.user.UserSummary;
import com.bidvibe.bidvibeapispring.entity.Item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response trả về thông tin chi tiết một vật phẩm.
 * Dùng cho GET /api/market/items, GET /api/items/inventory …
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private UUID id;
    private String name;
    private String description;
    private List<String> imageUrls;
    private List<String> tags;
    private Item.Rarity rarity;
    private Item.Status status;
    private Instant cooldownUntil;
    private Instant createdAt;

    /** Người ký gửi ban đầu. */
    private UserSummary seller;

    /** Chủ sở hữu hiện tại (null nếu chưa ai đấu giá thắng). */
    private UserSummary currentOwner;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .imageUrls(item.getImageUrls())
                .tags(item.getTags())
                .rarity(item.getRarity())
                .status(item.getStatus())
                .cooldownUntil(item.getCooldownUntil())
                .createdAt(item.getCreatedAt())
                .seller(item.getSeller() != null ? UserSummary.builder()
                        .id(item.getSeller().getId())
                        .nickname(item.getSeller().getNickname())
                        .avatarUrl(item.getSeller().getAvatarUrl())
                        .reputationScore(item.getSeller().getReputationScore())
                        .build() : null)
                .currentOwner(item.getCurrentOwner() != null ? UserSummary.builder()
                        .id(item.getCurrentOwner().getId())
                        .nickname(item.getCurrentOwner().getNickname())
                        .avatarUrl(item.getCurrentOwner().getAvatarUrl())
                        .reputationScore(item.getCurrentOwner().getReputationScore())
                        .build() : null)
                .build();
    }
}
