package com.bidvibe.bidvibeapispring.scheduler;

import com.bidvibe.bidvibeapispring.dto.ws.AuctionUpdatePayload;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.service.AuctionService;
import com.bidvibe.bidvibeapispring.service.WsEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Giảm giá Dutch Auction theo lịch.
 *
 * <p>Chạy mỗi giây; với mỗi DUTCH ACTIVE auction:
 * <ol>
 *   <li>Kiểm tra xem đã đến chu kỳ giảm giá chưa (dựa vào {@code intervalSeconds}).</li>
 *   <li>Nếu đến: giảm {@code currentPrice} xuống {@code decreaseAmount}.</li>
 *   <li>Nếu xuống tới {@code minPrice}: kết thúc auction (không có người thắng).</li>
 *   <li>Broadcast giá mới qua WebSocket.</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DutchPriceDropScheduler {

    private final AuctionRepository auctionRepository;
    private final AuctionService auctionService;
    private final WsEventPublisher wsEventPublisher;

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void dropPrices() {
        List<Auction> dutchActive = auctionRepository.findByStatus(Auction.Status.ACTIVE)
                .stream()
                .filter(a -> a.getSession().getType() == AuctionSession.Type.DUTCH)
                .toList();

        Instant now = Instant.now();

        for (Auction auction : dutchActive) {
            if (auction.getDecreaseAmount() == null || auction.getIntervalSeconds() == null) {
                continue;
            }

            // Tính số giây đã trôi từ lúc auction ACTIVE (dùng endTime - duration)
            // Cách đơn giản: kiểm tra (now.epochSecond % intervalSeconds == 0)
            long epochSec = now.getEpochSecond();
            if (epochSec % auction.getIntervalSeconds() != 0) {
                continue;
            }

            BigDecimal newPrice = auction.getCurrentPrice().subtract(auction.getDecreaseAmount());

            // Đã về minPrice hoặc dưới → kết thúc không có người mua
            if (auction.getMinPrice() != null && newPrice.compareTo(auction.getMinPrice()) <= 0) {
                log.info("[DutchPriceDropScheduler] Auction {} về giá sàn, kết thúc", auction.getId());
                auctionService.endAuction(auction);
                continue;
            }

            auction.setCurrentPrice(newPrice);
            auctionRepository.save(auction);

            log.debug("[DutchPriceDropScheduler] Auction {} giảm giá → {}", auction.getId(), newPrice);

            // Broadcast giá mới
            wsEventPublisher.publishAuctionUpdate(
                    auction.getId(),
                    AuctionUpdatePayload.builder()
                            .auctionId(auction.getId())
                            .currentPrice(newPrice)
                            .status(auction.getStatus())
                            .endTime(auction.getEndTime())
                            .totalBids(0)
                            .build());
        }
    }
}
