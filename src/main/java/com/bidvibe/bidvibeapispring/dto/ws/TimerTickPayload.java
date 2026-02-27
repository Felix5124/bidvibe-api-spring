package com.bidvibe.bidvibeapispring.dto.ws;

import com.bidvibe.bidvibeapispring.constant.WsEvents;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * WebSocket payload cho event: {@value WsEvents#TIMER_TICK}.
 *
 * <p>Destination: {@code /topic/timer/{auctionId}}
 *
 * <p>Server bắn đi mỗi giây để đồng bộ đồng hồ đếm ngược cho tất cả client.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerTickPayload {

    /** Tên event. */
    private final String event = WsEvents.TIMER_TICK;

    private UUID auctionId;

    /** Số giây còn lại. */
    private int remainingSeconds;
}
