package com.bidvibe.bidvibeapispring.dto.auction;

import java.time.Instant;

import com.bidvibe.bidvibeapispring.entity.AuctionSession;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho Admin tạo phiên đấu giá mới
 * (POST /api/admin/items/approve – bao gồm xếp lịch phiên).
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateAuctionSessionRequest {

    @NotBlank(message = "Tiêu đề phiên không được để trống")
    private String title;

    @NotNull(message = "Hình thức đấu giá không được để trống")
    private AuctionSession.Type type;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Future(message = "Thời gian bắt đầu phải ở tương lai")
    private Instant startTime;
}
