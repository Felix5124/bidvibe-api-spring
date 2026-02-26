package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Watchlist.WatchlistId> {

    /**
     * Danh sách auction user đang theo dõi (/api/users/watchlist).
     */
    List<Watchlist> findByUserId(UUID userId);

    /**
     * Tất cả user đang theo dõi một auction –
     * dùng để gửi thông báo toast/email khi phiên bắt đầu.
     */
    List<Watchlist> findByAuctionId(UUID auctionId);

    /**
     * Kiểm tra user đã theo dõi auction này chưa –
     * dùng để toggle watchlist (/api/users/watchlist/{id}).
     */
    boolean existsByUserIdAndAuctionId(UUID userId, UUID auctionId);

    /**
     * Xóa theo dõi khi user bấm toggle lần 2.
     */
    void deleteByUserIdAndAuctionId(UUID userId, UUID auctionId);

    /**
     * Đếm số người đang theo dõi một auction –
     * hiển thị trên UI.
     */
    long countByAuctionId(UUID auctionId);

    /**
     * Lấy danh sách user_id theo dõi auction – dùng để batch gửi notification.
     */
    @Query("SELECT w.user.id FROM Watchlist w WHERE w.auction.id = :auctionId")
    List<UUID> findUserIdsByAuctionId(@Param("auctionId") UUID auctionId);
}

