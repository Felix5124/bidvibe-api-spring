package com.bidvibe.bidvibeapispring.dto.auction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request body khi Admin từ chối một item.
 * POST /api/admin/items/{id}/reject
 */
@Getter
@NoArgsConstructor
public class RejectItemRequest {

    @NotBlank(message = "Lý do từ chối không được để trống")
    @Size(max = 500, message = "Lý do không được quá 500 ký tự")
    private String reason;
}
