package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    /**
     * Tất cả đánh giá nhận được của một user –
     * hiển thị trên trang cá nhân.
     */
    List<Rating> findByToUserIdOrderByIdDesc(UUID toUserId);

    /**
     * Tất cả đánh giá mà user đã gửi đi –
     * kiểm tra user đã rate chưa.
     */
    List<Rating> findByFromUserId(UUID fromUserId);

    /**
     * Tìm đánh giá cụ thể của một transaction (auction) –
     * đảm bảo mỗi giao dịch chỉ được đánh giá một lần.
     */
    Optional<Rating> findByFromUserIdAndAuctionId(UUID fromUserId, UUID auctionId);

    /**
     * Kiểm tra user đã đánh giá giao dịch này chưa –
     * dùng trước khi cho phép gọi /api/users/rate.
     */
    boolean existsByFromUserIdAndAuctionId(UUID fromUserId, UUID auctionId);

    /**
     * Tính điểm trung bình của một user –
     * đồng bộ với UserRepository.calculateAverageRating.
     */
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.toUser.id = :userId")
    Optional<Double> calculateAverageRating(@Param("userId") UUID userId);

    /**
     * Tổng số lượt đánh giá nhận được –
     * hiển thị cùng điểm trung bình trên profile.
     */
    long countByToUserId(UUID toUserId);
}

