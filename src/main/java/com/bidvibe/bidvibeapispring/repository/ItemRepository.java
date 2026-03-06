package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    /** Danh sách vật phẩm chờ Admin duyệt - không phân trang. */
    List<Item> findByStatus(Item.Status status);

    /** Danh sách vật phẩm theo status có phân trang (/api/admin/items). */
    Page<Item> findByStatus(Item.Status status, Pageable pageable);

    /** Kho đồ của người dùng – những món đã thắng đấu giá (/api/items/inventory). */
    Page<Item> findByCurrentOwnerIdAndStatus(UUID ownerId, Item.Status status, Pageable pageable);

    /** Tất cả đồ do một seller gửi ký gửi. */
    List<Item> findBySellerId(UUID sellerId);

    /** Kiểm tra xem item có đang trong thời gian cooldown không. */
    @Query("SELECT CASE WHEN i.cooldownUntil IS NOT NULL AND i.cooldownUntil > CURRENT_TIMESTAMP THEN TRUE ELSE FALSE END FROM Item i WHERE i.id = :itemId")
    boolean isInCooldown(@Param("itemId") UUID itemId);
}

