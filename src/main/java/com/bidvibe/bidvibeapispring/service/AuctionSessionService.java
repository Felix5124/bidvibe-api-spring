package com.bidvibe.bidvibeapispring.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionSessionResponse;
import com.bidvibe.bidvibeapispring.dto.auction.CreateAuctionSessionRequest;
import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionSessionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Quản lý phiên đấu giá lớn (AuctionSession):
 * - Admin tạo phiên
 * - Lấy danh sách theo trạng thái
 */
@Service
@RequiredArgsConstructor
public class AuctionSessionService {

    private final AuctionSessionRepository sessionRepository;

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    /** Tạo phiên đấu giá mới */
    @Transactional
    public AuctionSessionResponse createSession(CreateAuctionSessionRequest req) {
        AuctionSession session = sessionRepository.save(AuctionSession.builder()
                .title(req.getTitle())
                .type(req.getType())
                .startTime(req.getStartTime())
                .status(AuctionSession.Status.SCHEDULED)
                .build());
        return AuctionSessionResponse.from(session);
    }

    /** Chuyển trạng thái phiên sang ACTIVE */
    @Transactional
    public AuctionSessionResponse activateSession(UUID sessionId) {
        AuctionSession session = findById(sessionId);
        if (session.getStatus() != AuctionSession.Status.SCHEDULED) {
            throw new BidVibeException(ErrorCode.SESSION_ALREADY_STARTED);
        }
        session.setStatus(AuctionSession.Status.ACTIVE);
        return AuctionSessionResponse.from(sessionRepository.save(session));
    }

    /** Đánh dấu phiên COMPLETED */
    @Transactional
    public AuctionSessionResponse completeSession(UUID sessionId) {
        AuctionSession session = findById(sessionId);
        session.setStatus(AuctionSession.Status.COMPLETED);
        return AuctionSessionResponse.from(sessionRepository.save(session));
    }

    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AuctionSessionResponse> getSessionsByStatus(AuctionSession.Status status) {
        return sessionRepository.findByStatus(status)
                .stream().map(AuctionSessionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AuctionSessionResponse getSession(UUID sessionId) {
        return AuctionSessionResponse.from(findById(sessionId));
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    public AuctionSession findById(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.SESSION_NOT_FOUND));
    }
}
