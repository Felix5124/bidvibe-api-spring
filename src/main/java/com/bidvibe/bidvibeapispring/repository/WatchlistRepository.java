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
     * Danh sách item user đang theo dõi (/api/users/watchlist).
     */
    List<Watchlist> findByUserId(UUID userId);

    /**
     * Tất cả user đang theo dõi một item —
     * dùng để gửi thông báo khi phiên đấu giá item đó bắt đầu.
     */
    List<Watchlist> findByItemId(UUID itemId);

    /**
     * Kiểm tra user đã theo dõi item này chưa —
     * dùng để toggle watchlist.
     */
    boolean existsByUserIdAndItemId(UUID userId, UUID itemId);

    /**
     * Xóa theo dõi khi user bấm toggle lần 2.
     */
    void deleteByUserIdAndItemId(UUID userId, UUID itemId);

    /**
     * Đếm số người đang theo dõi một item.
     */
    long countByItemId(UUID itemId);

    /**
     * Lấy danh sách user_id theo dõi item — dùng để batch gửi notification.
     */
    @Query("SELECT w.user.id FROM Watchlist w WHERE w.item.id = :itemId")
    List<UUID> findUserIdsByItemId(@Param("itemId") UUID itemId);
}

