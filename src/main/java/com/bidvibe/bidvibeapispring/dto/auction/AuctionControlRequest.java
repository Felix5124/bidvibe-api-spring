package com.bidvibe.bidvibeapispring.dto.auction;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/admin/auctions/control.
 * Admin start / stop / pause một phiên đấu giá.
 */
@Getter
@Setter
@NoArgsConstructor
public class AuctionControlRequest {

    @NotNull(message = "Hành động điều khiển không được để trống")
    private Action action;

    public enum Action {
        START,
        PAUSE,
        RESUME,
        STOP,
        CANCEL
    }
}
