package com.bidvibe.bidvibeapispring.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request body cho POST /api/admin/users/{id}/kick.
 */
@Getter
@NoArgsConstructor
public class KickUserRequest {

    @NotNull(message = "auctionId không được để trống")
    private UUID auctionId;
}
