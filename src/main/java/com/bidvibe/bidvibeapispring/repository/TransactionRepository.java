package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Lịch sử giao dịch của ví, mới nhất trước (/api/wallet/history).
     */
    Page<Transaction> findByWalletIdOrderByTimestampDesc(UUID walletId, Pageable pageable);

    /**
     * Lọc theo loại giao dịch (DEPOSIT, WITHDRAW, …).
     */
    Page<Transaction> findByWalletIdAndTypeOrderByTimestampDesc(
            UUID walletId, Transaction.Type type, Pageable pageable);

    /**
     * Tất cả giao dịch PENDING cần Admin duyệt (/api/admin/finance/approve).
     * Ưu tiên DEPOSIT và WITHDRAW.
     */
    List<Transaction> findByTypeInAndStatusOrderByTimestampAsc(
            List<Transaction.Type> types, Transaction.Status status);

    /**
     * Lịch sử giao dịch theo wallet + loại + trạng thái –
     * dùng để đối soát khi giải quyết tranh chấp.
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.wallet.id = :walletId
              AND (:type IS NULL OR t.type = :type)
              AND (:status IS NULL OR t.status = :status)
            ORDER BY t.timestamp DESC
            """)
    Page<Transaction> findByWalletFiltered(
            @Param("walletId") UUID walletId,
            @Param("type") Transaction.Type type,
            @Param("status") Transaction.Status status,
            Pageable pageable);

    /**
     * Lấy các BID_LOCK chưa được unlock của một user trong một auction –
     * dùng để unlock tiền khi người dùng bị outbid.
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.wallet.user.id = :userId
              AND t.type = 'BID_LOCK'
              AND t.status = 'COMPLETED'
              AND NOT EXISTS (
                  SELECT 1 FROM Transaction t2
                  WHERE t2.wallet = t.wallet
                    AND t2.type = 'BID_UNLOCK'
                    AND t2.status = 'COMPLETED'
              )
            ORDER BY t.timestamp DESC
            """)
    List<Transaction> findUnresolvedBidLocks(@Param("userId") UUID userId);
}

