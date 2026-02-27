package com.bidvibe.bidvibeapispring.dto.rating;

import com.bidvibe.bidvibeapispring.constant.AppConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request body cho POST /api/users/rate.
 * Đánh giá sao và nhận xét sau giao dịch thành công.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateRatingRequest {

    @NotNull(message = "ID người được đánh giá không được để trống")
    private UUID toUserId;

    @NotNull(message = "ID phiên đấu giá không được để trống")
    private UUID auctionId;

    @NotNull(message = "Số sao không được để trống")
    @Min(value = AppConstants.MIN_RATING_STARS, message = "Số sao tối thiểu là 1")
    @Max(value = AppConstants.MAX_RATING_STARS, message = "Số sao tối đa là 5")
    private Integer stars;

    @Size(max = 1000, message = "Nhận xét không được quá 1000 ký tự")
    private String comment;
}
