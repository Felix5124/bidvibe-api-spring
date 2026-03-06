package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    /** Admin: tìm kiếm user theo email/nickname, lọc theo role, is_banned, is_muted. */
    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:role    IS NULL OR u.role     = :role)
              AND (:isBanned IS NULL OR u.isBanned = :isBanned)
              AND (:isMuted  IS NULL OR u.isMuted  = :isMuted)
            ORDER BY u.createdAt DESC
            """)
    Page<User> searchUsers(
            @Param("search")   String search,
            @Param("role")     User.Role role,
            @Param("isBanned") Boolean isBanned,
            @Param("isMuted")  Boolean isMuted,
            Pageable pageable);}

