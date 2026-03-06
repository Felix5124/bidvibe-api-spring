package com.bidvibe.bidvibeapispring.scheduler;

import com.bidvibe.bidvibeapispring.dto.ws.TimerTickPayload;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.service.WsEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Broadcast đồng hồ đếm ngược mỗi giây tới tất cả phòng đấu giá đang ACTIVE.
 * Client subscribe /topic/timer/{auctionId} để nhận TimerTickPayload.
 */
@Component
@RequiredArgsConstructor
public class TimerBroadcastScheduler {

    private final AuctionRepository auctionRepository;
    private final WsEventPublisher wsEventPublisher;

    @Scheduled(fixedRate = 1000)
    public void broadcastTimerTicks() {
        List<Auction> active = auctionRepository.findByStatus(Auction.Status.ACTIVE);
        Instant now = Instant.now();
        for (Auction auction : active) {
            if (auction.getEndTime() == null) continue;
            long remaining = ChronoUnit.SECONDS.between(now, auction.getEndTime());
            wsEventPublisher.publishTimerTick(
                    auction.getId(),
                    TimerTickPayload.builder()
                            .auctionId(auction.getId())
                            .remainingSeconds((int) Math.max(0, remaining))
                            .build());
        }
    }
}
