package com.bidvibe.bidvibeapispring.repository;

import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuctionSessionRepository extends JpaRepository<AuctionSession, UUID> {

    /** Danh sách phiên theo trạng thái (vd: SCHEDULED, ACTIVE). */
    List<AuctionSession> findByStatus(AuctionSession.Status status);

    /** Tìm phiên theo loại hình đấu giá. */
    List<AuctionSession> findByType(AuctionSession.Type type);

    /** Tìm các phiên được lên lịch sau thời điểm nhất định. */
    List<AuctionSession> findByStatusAndStartTimeAfter(AuctionSession.Status status, Instant after);

    /** Tìm phiên đang ACTIVE theo loại – dùng khi Admin điều hành phiên. */
    List<AuctionSession> findByStatusAndType(AuctionSession.Status status, AuctionSession.Type type);
}

