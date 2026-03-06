package com.bidvibe.bidvibeapispring.dto.user;

import com.bidvibe.bidvibeapispring.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho PATCH /api/admin/users/{id}/role.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChangeRoleRequest {

    @NotNull(message = "Role không được để trống")
    private User.Role role;
}
