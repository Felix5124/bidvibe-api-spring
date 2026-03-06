package com.bidvibe.bidvibeapispring.scheduler;

import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Kiểm tra và kết thúc các phiên đấu giá đã hết giờ.
 * Chạy mỗi giây, tìm ACTIVE auction có endTime ≤ now.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEndScheduler {

    private final AuctionRepository auctionRepository;
    private final AuctionService auctionService;

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void checkExpiredAuctions() {
        List<Auction> active = auctionRepository.findByStatus(Auction.Status.ACTIVE);
        Instant now = Instant.now();
        for (Auction auction : active) {
            if (auction.getEndTime() != null && !auction.getEndTime().isAfter(now)) {
                log.info("[AuctionEndScheduler] Kết thúc auction {} (endTime={})",
                        auction.getId(), auction.getEndTime());
                auctionService.endAuction(auction);
            }
        }
    }
}
