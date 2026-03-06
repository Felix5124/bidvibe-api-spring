package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.entity.MarketListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketListingRepository extends JpaRepository<MarketListing, UUID> {

    /**
     * Listing ACTIVE của một item – DB đảm bảo partial unique index (chỉ 1 ACTIVE per item).
     * Dùng để check trước khi tạo listing mới.
     */
    Optional<MarketListing> findByItemIdAndStatus(UUID itemId, MarketListing.Status status);

    /**
     * Tất cả listing ACTIVE trên Chợ Đen (/api/market/listings).
     */
    Page<MarketListing> findByStatus(MarketListing.Status status, Pageable pageable);

    /**
     * Tìm kiếm & lọc Chợ Đen theo tên item, độ hiếm – hỗ trợ bộ lọc thông minh.
     */
    @Query("""
            SELECT l FROM MarketListing l
            WHERE l.status = 'ACTIVE'
              AND (:keyword IS NULL OR LOWER(l.item.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:rarity IS NULL OR l.item.rarity = :rarity)
            ORDER BY l.createdAt DESC
            """)
    Page<MarketListing> searchActive(
            @Param("keyword") String keyword,
            @Param("rarity") Item.Rarity rarity,
            Pageable pageable);

    /**
     * Lịch sử listing của một seller.
     */
    Page<MarketListing> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);

    /**
     * Kiểm tra item có đang được rao bán (listing ACTIVE) không.
     */
    boolean existsByItemIdAndStatus(UUID itemId, MarketListing.Status status);
}
