package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {

    /** Tất cả auctions trong một phiên, sắp theo thứ tự. */
    List<Auction> findBySessionIdOrderByOrderIndex(UUID sessionId);

    /** Auction đang ACTIVE trong một phiên (dùng cho Real-time room). */
    Optional<Auction> findBySessionIdAndStatus(UUID sessionId, Auction.Status status);

    /**
     * Tìm auction của một item cụ thể – dùng để check trạng thái
     * và cập nhật giá khi có bid.
     */
    Optional<Auction> findByItemId(UUID itemId);

    /** Tất cả auctions theo trạng thái (vd: lấy toàn bộ ACTIVE để scheduler xử lý). */
    List<Auction> findByStatus(Auction.Status status);

    /**
     * Danh sách auctions đã kết thúc (ENDED) của một phiên –
     * dùng cho lịch sử giá và phân tích.
     */
    Page<Auction> findBySessionIdAndStatus(UUID sessionId, Auction.Status status, Pageable pageable);

    /**
     * Lấy auction đang ACTIVE cần xử lý Popcorn Bidding:
     * những auction có end_time sắp hết hạn.
     */
    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' AND a.endTime BETWEEN :from AND :to")
    List<Auction> findActiveAuctionsEndingBetween(
            @Param("from") Instant from,
            @Param("to") Instant to);

    /**
     * Lịch sử giá của một item để vẽ biểu đồ (/api/analytics/price/{id}).
     * Lấy tất cả auctions của item đó đã ENDED, sắp theo thời gian.
     */
    @Query("SELECT a FROM Auction a WHERE a.item.id = :itemId AND a.status = 'ENDED' ORDER BY a.endTime ASC")
    List<Auction> findPriceHistoryByItemId(@Param("itemId") UUID itemId);

    /**
     * Tìm auction ACTIVE tiếp theo trong phiên (đấu giá tuần tự).
     * Lấy auction có order_index nhỏ nhất đang WAITING.
     */
    @Query("SELECT a FROM Auction a WHERE a.session.id = :sessionId AND a.status = 'WAITING' ORDER BY a.orderIndex ASC LIMIT 1")
    Optional<Auction> findNextWaitingInSession(@Param("sessionId") UUID sessionId);
}

