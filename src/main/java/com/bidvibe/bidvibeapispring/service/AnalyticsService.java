package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.analytics.AdminAuctionStatsResponse;
import com.bidvibe.bidvibeapispring.dto.analytics.AdminMarketStatsResponse;
import com.bidvibe.bidvibeapispring.dto.analytics.AdminOverviewResponse;
import com.bidvibe.bidvibeapispring.dto.analytics.AdminRevenueResponse;
import com.bidvibe.bidvibeapispring.dto.analytics.PriceHistoryResponse;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.Bid;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.entity.MarketListing;
import com.bidvibe.bidvibeapispring.entity.Transaction;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.repository.BidRepository;
import com.bidvibe.bidvibeapispring.repository.ItemRepository;
import com.bidvibe.bidvibeapispring.repository.MarketListingRepository;
import com.bidvibe.bidvibeapispring.repository.TransactionRepository;
import com.bidvibe.bidvibeapispring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Xử lý dữ liệu phân tích giá và thống kê admin.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ItemRepository itemRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final MarketListingRepository marketListingRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ------------------------------------------------------------------
    // Public
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PriceHistoryResponse getPriceHistory(UUID itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.ITEM_NOT_FOUND));

        var auctions = auctionRepository.findByItemIdOrderByEndTimeDesc(itemId);
        if (auctions.isEmpty()) throw new BidVibeException(ErrorCode.AUCTION_NOT_FOUND);
        var auction = auctions.get(0);

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

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    /** GET /api/admin/analytics/overview */
    @Transactional(readOnly = true)
    public AdminOverviewResponse getAdminOverview() {
        long totalUsers = userRepository.count();
        long totalItems = itemRepository.count();
        long totalAuctions = auctionRepository.count();
        long activeAuctions = auctionRepository.findByStatus(Auction.Status.ACTIVE).size();
        long totalListings = marketListingRepository.count();
        long activeListings = marketListingRepository
                .findByStatus(MarketListing.Status.ACTIVE, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();

        BigDecimal totalRevenue = transactionRepository
                .findAllFiltered(Transaction.Type.FINAL_PAYMENT, Transaction.Status.COMPLETED,
                        org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingDeposits = transactionRepository
                .findByTypeInAndStatusOrderByCreatedAtAsc(
                        List.of(Transaction.Type.DEPOSIT), Transaction.Status.PENDING)
                .size();
        long pendingWithdrawals = transactionRepository
                .findByTypeInAndStatusOrderByCreatedAtAsc(
                        List.of(Transaction.Type.WITHDRAW), Transaction.Status.PENDING)
                .size();

        return AdminOverviewResponse.builder()
                .totalUsers(totalUsers)
                .totalItems(totalItems)
                .totalAuctions(totalAuctions)
                .activeAuctions(activeAuctions)
                .totalMarketListings(totalListings)
                .activeMarketListings(activeListings)
                .totalRevenue(totalRevenue)
                .pendingDeposits(pendingDeposits)
                .pendingWithdrawals(pendingWithdrawals)
                .build();
    }

    /** GET /api/admin/analytics/revenue?from=&to= */
    @Transactional(readOnly = true)
    public AdminRevenueResponse getAdminRevenue(LocalDate from, LocalDate to) {
        var allPayments = transactionRepository
                .findAllFiltered(Transaction.Type.FINAL_PAYMENT, Transaction.Status.COMPLETED,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        // Group by date
        Map<LocalDate, List<Transaction>> byDate = allPayments.stream()
                .filter(t -> {
                    LocalDate d = t.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
                    return !d.isBefore(from) && !d.isAfter(to);
                })
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate()));

        List<AdminRevenueResponse.RevenuePoint> daily = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> AdminRevenueResponse.RevenuePoint.builder()
                        .date(e.getKey())
                        .revenue(e.getValue().stream()
                                .map(Transaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .transactionCount(e.getValue().size())
                        .build())
                .toList();

        BigDecimal total = daily.stream()
                .map(AdminRevenueResponse.RevenuePoint::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminRevenueResponse.builder()
                .totalRevenue(total)
                .dailyRevenue(daily)
                .build();
    }

    /** GET /api/admin/analytics/auctions */
    @Transactional(readOnly = true)
    public AdminAuctionStatsResponse getAdminAuctionStats() {
        long total = auctionRepository.count();
        long ended = auctionRepository.findByStatus(Auction.Status.ENDED).size();
        long cancelled = auctionRepository.findByStatus(Auction.Status.CANCELLED).size();
        long active = auctionRepository.findByStatus(Auction.Status.ACTIVE).size();
        long totalBids = bidRepository.count();

        List<Transaction> finalPayments = transactionRepository
                .findAllFiltered(Transaction.Type.FINAL_PAYMENT, Transaction.Status.COMPLETED,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        BigDecimal totalVolume = finalPayments.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgWinningPrice = finalPayments.isEmpty() ? BigDecimal.ZERO
                : totalVolume.divide(BigDecimal.valueOf(finalPayments.size()),
                        2, java.math.RoundingMode.HALF_UP);

        return AdminAuctionStatsResponse.builder()
                .totalAuctions(total)
                .endedAuctions(ended)
                .cancelledAuctions(cancelled)
                .activeAuctions(active)
                .totalBids(totalBids)
                .avgWinningPrice(avgWinningPrice)
                .totalVolume(totalVolume)
                .build();
    }

    /** GET /api/admin/analytics/market */
    @Transactional(readOnly = true)
    public AdminMarketStatsResponse getAdminMarketStats() {
        long total = marketListingRepository.count();
        long sold = marketListingRepository
                .findByStatus(MarketListing.Status.SOLD, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        long cancelled = marketListingRepository
                .findByStatus(MarketListing.Status.CANCELLED, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        long active = marketListingRepository
                .findByStatus(MarketListing.Status.ACTIVE, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();

        // Volume từ tất cả bản ghi SOLD
        List<MarketListing> soldListings = marketListingRepository
                .findByStatus(MarketListing.Status.SOLD, org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        BigDecimal totalVolume = soldListings.stream()
                .map(MarketListing::getAskingPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgSalePrice = soldListings.isEmpty() ? BigDecimal.ZERO
                : totalVolume.divide(BigDecimal.valueOf(soldListings.size()),
                        2, java.math.RoundingMode.HALF_UP);

        return AdminMarketStatsResponse.builder()
                .totalListings(total)
                .soldListings(sold)
                .cancelledListings(cancelled)
                .activeListings(active)
                .avgSalePrice(avgSalePrice)
                .totalVolume(totalVolume)
                .build();
    }
}
