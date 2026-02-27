package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.analytics.PriceHistoryResponse;
import com.bidvibe.bidvibeapispring.entity.Bid;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.repository.BidRepository;
import com.bidvibe.bidvibeapispring.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Xử lý dữ liệu phân tích giá:
 * GET /api/analytics/price/{itemId} – dữ liệu vẽ biểu đồ lịch sử giá.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ItemRepository itemRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Transactional(readOnly = true)
    public PriceHistoryResponse getPriceHistory(UUID itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.ITEM_NOT_FOUND));

        // Tìm auction của item (có thể đã kết thúc)
        var auction = auctionRepository.findByItemId(itemId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.AUCTION_NOT_FOUND));

        List<Bid> bids = bidRepository.findByAuctionIdOrderByBidTimeDesc(auction.getId());

        List<PriceHistoryResponse.PricePoint> points = bids.stream()
                .sorted((a, b) -> a.getBidTime().compareTo(b.getBidTime()))
                .map(bid -> PriceHistoryResponse.PricePoint.builder()
                        .timestamp(bid.getBidTime())
                        .price(bid.getAmount())
                        .eventType(bid.isProxy() ? "PROXY_BID" : "BID")
                        .build())
                .toList();

        return PriceHistoryResponse.builder()
                .itemId(itemId)
                .itemName(item.getName())
                .pricePoints(points)
                .build();
    }
}
