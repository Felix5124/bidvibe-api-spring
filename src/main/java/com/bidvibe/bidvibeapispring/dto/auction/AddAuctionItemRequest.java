package com.bidvibe.bidvibeapispring.dto.auction;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request body khi Admin thêm item vào một phiên đấu giá.
 * POST /api/admin/sessions/{id}/auctions
 */
@Getter
@NoArgsConstructor
public class AddAuctionItemRequest {

    @NotNull(message = "itemId không được để trống")
    private UUID itemId;
}
