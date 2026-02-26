package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /** Lấy ví theo user_id. */
    Optional<Wallet> findByUserId(UUID userId);

    /**
     * Lấy ví với PESSIMISTIC_WRITE lock để an toàn khi cộng/trừ tiền
     * trong những tình huống cần đảm bảo tuyệt đối (ngoài Optimistic Locking).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
    Optional<Wallet> findByUserIdWithLock(@Param("userId") UUID userId);
}

