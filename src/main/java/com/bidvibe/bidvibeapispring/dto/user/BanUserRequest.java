package com.bidvibe.bidvibeapispring.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/admin/users/{id}/ban.
 */
@Getter
@Setter
@NoArgsConstructor
public class BanUserRequest {

    @Size(max = 500, message = "Lý do không được quá 500 ký tự")
    private String reason;
}
