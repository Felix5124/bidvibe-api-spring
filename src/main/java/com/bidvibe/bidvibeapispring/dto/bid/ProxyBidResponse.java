package com.bidvibe.bidvibeapispring.dto.bid;

import com.bidvibe.bidvibeapispring.entity.ProxyBid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response trả về cấu hình Proxy Bid hiện tại của user trong một auction.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyBidResponse {

    private UUID id;
    private UUID auctionId;
    private UUID userId;
    private BigDecimal maxAmount;
    private boolean active;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static ProxyBidResponse from(ProxyBid proxyBid) {
        return ProxyBidResponse.builder()
                .id(proxyBid.getId())
                .auctionId(proxyBid.getAuction().getId())
                .userId(proxyBid.getUser().getId())
                .maxAmount(proxyBid.getMaxAmount())
                .active(proxyBid.isActive())
                .build();
    }
}
