package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    /** Lịch sử toàn bộ bid của một auction, mới nhất trước. */
    List<Bid> findByAuctionIdOrderByBidTimeDesc(UUID auctionId);

    /** Lịch sử bid phân trang của một auction. */
    Page<Bid> findByAuctionId(UUID auctionId, Pageable pageable);

    /**
     * Bid cao nhất hiện tại trong auction –
     * dùng để xác định người dẫn đầu và current_price.
     */
    @Query("SELECT b FROM Bid b WHERE b.auction.id = :auctionId ORDER BY b.amount DESC LIMIT 1")
    Optional<Bid> findHighestBidInAuction(@Param("auctionId") UUID auctionId);

    /**
     * Lịch sử bid của một user trong một auction –
     * dùng để kiểm tra outbid và hiển thị trong phòng đấu giá.
     */
    List<Bid> findByAuctionIdAndUserIdOrderByBidTimeDesc(UUID auctionId, UUID userId);

    /**
     * Kiểm tra xem user có bid trong auction này không –
     * dùng để validate trước khi đặt Sealed Bid.
     */
    boolean existsByAuctionIdAndUserId(UUID auctionId, UUID userId);

    /**
     * Tổng số bid của một auction –
     * hiển thị trên UI phòng đấu giá.
     */
    long countByAuctionId(UUID auctionId);

    /**
     * Lấy tất cả bid do Admin cần xem khi xử lý tranh chấp.
     * Bao gồm cả proxy bids.
     */
    @Query("SELECT b FROM Bid b WHERE b.auction.id = :auctionId ORDER BY b.bidTime ASC")
    List<Bid> findAllBidsForDispute(@Param("auctionId") UUID auctionId);

    /**
     * Lấy bid cao nhất trước bid hiện tại của user –
     * dùng trong Proxy Bidding để tính bước nhảy tiếp theo.
     */
    @Query("""
            SELECT b FROM Bid b
            WHERE b.auction.id = :auctionId
              AND b.user.id <> :userId
            ORDER BY b.amount DESC
            LIMIT 1
            """)
    Optional<Bid> findHighestBidByOtherUser(
            @Param("auctionId") UUID auctionId,
            @Param("userId") UUID userId);
}

