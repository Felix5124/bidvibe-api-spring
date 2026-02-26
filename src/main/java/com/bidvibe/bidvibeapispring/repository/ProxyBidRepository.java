package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.ProxyBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProxyBidRepository extends JpaRepository<ProxyBid, UUID> {

    /**
     * Lấy cấu hình proxy bid đang active của một user trong auction –
     * dùng để tự động nâng giá hộ người dùng.
     */
    Optional<ProxyBid> findByAuctionIdAndUserIdAndIsActiveTrue(UUID auctionId, UUID userId);

    /**
     * Tất cả proxy bids đang active trong một auction –
     * dùng khi engine cần so sánh và kích hoạt auto-bid.
     */
    List<ProxyBid> findByAuctionIdAndIsActiveTrue(UUID auctionId);

    /**
     * Kiểm tra user đã có proxy bid trong auction chưa.
     */
    boolean existsByAuctionIdAndUserId(UUID auctionId, UUID userId);

    /**
     * Tất cả proxy bids của một user trên nhiều auctions –
     * dùng khi user muốn quản lý / hủy các proxy bid của mình.
     */
    List<ProxyBid> findByUserIdAndIsActiveTrue(UUID userId);
}

