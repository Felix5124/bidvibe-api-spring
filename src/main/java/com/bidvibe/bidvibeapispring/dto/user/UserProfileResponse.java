package com.bidvibe.bidvibeapispring.dto.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.bidvibe.bidvibeapispring.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response trả về thông tin profile người dùng.
 * Endpoint: GET /api/users/profile
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String address;
    private BigDecimal reputationScore;
    private User.Role role;
    private Instant createdAt;

    // ------------------------------------------------------------------
    // Mapper helper
    // ------------------------------------------------------------------

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .address(user.getAddress())
                .reputationScore(user.getReputationScore())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
