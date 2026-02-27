package com.bidvibe.bidvibeapispring.dto.user;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Thông tin tóm tắt của một người dùng – dùng làm field lồng ghép
 * trong các response khác (ví dụ BidResponse, AuctionResponse…).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {

    private UUID id;
    private String nickname;
    private String avatarUrl;
    private BigDecimal reputationScore;
}
