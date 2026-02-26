package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Chat Live trong phòng đấu giá – tin nhắn public của một auction room.
     * receiver là null khi là chat live.
     */
    List<Message> findByAuctionIdAndReceiverIsNullOrderByTimestampAsc(UUID auctionId);

    /**
     * Chat P2P thương lượng giữa 2 user (/api/market/chat-history).
     * auction là null khi là chat P2P.
     * Lấy cả chiều gửi và nhận.
     */
    @Query("""
            SELECT m FROM Message m
            WHERE m.auction IS NULL
              AND ((m.sender.id = :userId AND m.receiver.id = :otherId)
                OR (m.sender.id = :otherId AND m.receiver.id = :userId))
            ORDER BY m.timestamp ASC
            """)
    List<Message> findP2PConversation(
            @Param("userId") UUID userId,
            @Param("otherId") UUID otherId);

    /**
     * Lấy lịch sử chat P2P phân trang – dùng cho UI tải thêm tin cũ.
     */
    @Query("""
            SELECT m FROM Message m
            WHERE m.auction IS NULL
              AND ((m.sender.id = :userId AND m.receiver.id = :otherId)
                OR (m.sender.id = :otherId AND m.receiver.id = :userId))
            ORDER BY m.timestamp DESC
            """)
    Page<Message> findP2PConversationPaged(
            @Param("userId") UUID userId,
            @Param("otherId") UUID otherId,
            Pageable pageable);

    /**
     * Danh sách các cuộc hội thoại P2P gần nhất của user –
     * lấy tin nhắn cuối cùng của mỗi cuộc trò chuyện.
     */
    @Query("""
            SELECT m FROM Message m
            WHERE m.auction IS NULL
              AND (m.sender.id = :userId OR m.receiver.id = :userId)
            ORDER BY m.timestamp DESC
            """)
    Page<Message> findRecentP2PConversations(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Chat Live phân trang – Admin dùng để review khi moderation.
     */
    Page<Message> findByAuctionIdAndReceiverIsNull(UUID auctionId, Pageable pageable);
}

