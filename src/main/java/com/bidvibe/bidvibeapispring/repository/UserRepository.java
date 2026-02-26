package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Tìm user theo email (dùng khi xác thực JWT từ Supabase). */
    Optional<User> findByEmail(String email);

    /** Kiểm tra email đã tồn tại chưa. */
    boolean existsByEmail(String email);

    /**
     * Tính điểm uy tín trung bình từ bảng ratings để cập nhật lên profile.
     * Trả về null nếu chưa có đánh giá nào.
     */
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.toUser.id = :userId")
    Double calculateAverageRating(@Param("userId") UUID userId);
}

