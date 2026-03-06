package com.bidvibe.bidvibeapispring.scheduler;

import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Mở thầu Sealed Auction khi hết thời gian.
 *
 * <p>Chạy mỗi giây; tìm SEALED ACTIVE auction có {@code endTime ≤ now}
 * rồi gọi {@link BidService#openSealedBids(java.util.UUID)} để:
 * <ul>
 *   <li>Xác định bid cao nhất → winner.</li>
 *   <li>Unlock tiền những người thua.</li>
 *   <li>Thanh toán final + chuyển item cho winner.</li>
 *   <li>Broadcast kết quả qua WebSocket.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SealedRevealScheduler {

    private final AuctionRepository auctionRepository;
    private final BidService bidService;

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void revealExpiredSealedAuctions() {
        List<Auction> sealedActive = auctionRepository.findByStatus(Auction.Status.ACTIVE)
                .stream()
                .filter(a -> a.getSession().getType() == AuctionSession.Type.SEALED)
                .toList();

        Instant now = Instant.now();

        for (Auction auction : sealedActive) {
            if (auction.getEndTime() != null && !auction.getEndTime().isAfter(now)) {
                log.info("[SealedRevealScheduler] Mở thầu sealed auction {}", auction.getId());
                bidService.openSealedBids(auction.getId());
            }
        }
    }
}
