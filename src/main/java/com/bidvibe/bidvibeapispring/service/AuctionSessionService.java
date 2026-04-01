package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.AppConstants;
import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.auction.ApproveItemRequest;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionResponse;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionSessionResponse;
import com.bidvibe.bidvibeapispring.dto.auction.CreateAuctionSessionRequest;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.repository.AuctionSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionSessionService {

    private final AuctionSessionRepository sessionRepository;
    private final AuctionRepository auctionRepository;
    private final ItemService itemService;

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

    /** Admin thêm item vào phiên – tạo Auction record. */
    @Transactional
    public AuctionResponse addItemToSession(UUID sessionId, ApproveItemRequest req) {
        AuctionSession session = findById(sessionId);
        if (session.getStatus() != AuctionSession.Status.SCHEDULED) {
            throw new BidVibeException(ErrorCode.SESSION_ALREADY_STARTED);
        }

        if (session.getType() == AuctionSession.Type.DUTCH) {
            if (req.getMinPrice() == null || req.getMinPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BidVibeException(ErrorCode.DUTCH_AUCTION_REQUIRES_MIN_PRICE);
            }
        }

        if (auctionRepository.findActiveByItemId(req.getItemId()).isPresent()) {
            throw new BidVibeException(ErrorCode.ITEM_ALREADY_IN_SESSION);
        }

        Item item = itemService.approveAndMoveToAuction(req.getItemId(), req.getRarity());

        Auction auction = auctionRepository.save(Auction.builder()
                .session(session)
                .item(item)
                .startPrice(req.getStartPrice())
                .currentPrice(req.getStartPrice())
                .minPrice(req.getMinPrice())
                .stepPrice(req.getStepPrice())
                .decreaseAmount(req.getDecreaseAmount())
                .intervalSeconds(AppConstants.DUTCH_PRICE_DECREASE_INTERVAL_SECONDS)
                .endTime(req.getEndTime())
                .orderIndex(Objects.requireNonNullElse(req.getOrderIndex(), 0))
                .status(Auction.Status.WAITING)
                .build());

        return AuctionResponse.from(auction);
    }

    /** Admin xóa item khỏi phiên (chỉ khi SCHEDULED). */
    @Transactional
    public void removeItemFromSession(UUID sessionId, UUID auctionId) {
        AuctionSession session = findById(sessionId);
        if (session.getStatus() != AuctionSession.Status.SCHEDULED) {
            throw new BidVibeException(ErrorCode.SESSION_ALREADY_STARTED);
        }
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.AUCTION_NOT_FOUND));
        itemService.moveBackToApprovedFromAuction(auction.getItem().getId());
        auctionRepository.delete(auction);
    }

    /** Chỉnh trạng thái phiên sang ACTIVE */
    @Transactional
    public AuctionSessionResponse activateSession(UUID sessionId) {
        AuctionSession session = findById(sessionId);
        if (session.getStatus() != AuctionSession.Status.SCHEDULED) {
            throw new BidVibeException(ErrorCode.SESSION_ALREADY_STARTED);
        }
        session.setStatus(AuctionSession.Status.ACTIVE);
        return AuctionSessionResponse.from(sessionRepository.save(session));
    }

    /** PAUSE phiên – lưu remaining_seconds. */
    @Transactional
    public AuctionSessionResponse pauseSession(UUID sessionId) {
        AuctionSession session = findById(sessionId);
        if (session.getStatus() != AuctionSession.Status.ACTIVE) {
            throw new BidVibeException(ErrorCode.SESSION_NOT_ACTIVE);
        }
        session.setStatus(AuctionSession.Status.PAUSED);
        auctionRepository.findBySessionIdAndStatus(sessionId, Auction.Status.ACTIVE)
                .ifPresent(a -> {
                    long remaining = a.getEndTime() != null
                            ? Math.max(0, a.getEndTime().getEpochSecond() - Instant.now().getEpochSecond())
                            : AppConstants.ENGLISH_AUCTION_DURATION_SECONDS;
                    session.setRemainingSeconds((int) remaining);
                    a.setStatus(Auction.Status.WAITING);
                    auctionRepository.save(a);
                });
        return AuctionSessionResponse.from(sessionRepository.save(session));
    }

    /** RESUME phiên từ remaining_seconds. */
    @Transactional
    public AuctionSessionResponse resumeSession(UUID sessionId) {
        AuctionSession session = findById(sessionId);
        if (session.getStatus() != AuctionSession.Status.PAUSED) {
            throw new BidVibeException(ErrorCode.SESSION_NOT_PAUSED);
        }
        session.setStatus(AuctionSession.Status.ACTIVE);
        int remainingSeconds = session.getRemainingSeconds() != null 
                ? session.getRemainingSeconds() 
                : AppConstants.ENGLISH_AUCTION_DURATION_SECONDS;
        final int finalRemaining = remainingSeconds;
        auctionRepository.findBySessionIdAndStatus(sessionId, Auction.Status.WAITING)
                .ifPresent(a -> {
                    a.setEndTime(Instant.now().plusSeconds(finalRemaining));
                    a.setStatus(Auction.Status.ACTIVE);
                    auctionRepository.save(a);
                });
        session.setRemainingSeconds(null);
        return AuctionSessionResponse.from(sessionRepository.save(session));
    }

    /** Dừng phiên sớm – CANCELLED. */
    @Transactional
    public AuctionSessionResponse stopSession(UUID sessionId) {
        AuctionSession session = findById(sessionId);
        session.setStatus(AuctionSession.Status.CANCELLED);
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

    /** GET /api/sessions – danh sách phiên có lọc + phân trang. */
    @Transactional(readOnly = true)
    public Page<AuctionSessionResponse> listSessions(AuctionSession.Status status,
                                                      AuctionSession.Type type,
                                                      Pageable pageable) {
        return sessionRepository.searchSessions(status, type, pageable)
                .map(AuctionSessionResponse::from);
    }

    /** GET /api/sessions/{id} */
    @Transactional(readOnly = true)
    public AuctionSessionResponse getSession(UUID sessionId) {
        return AuctionSessionResponse.from(findById(sessionId));
    }

    /** GET /api/sessions/{id}/auctions – danh sách auction trong phiên theo thứ tự. */
    @Transactional(readOnly = true)
    public List<AuctionResponse> getSessionAuctions(UUID sessionId) {
        findById(sessionId); // validate exists
        return auctionRepository.findBySessionIdOrderByOrderIndex(sessionId)
                .stream().map(AuctionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AuctionSessionResponse> getSessionsByStatus(AuctionSession.Status status) {
        return sessionRepository.findByStatus(status)
                .stream().map(AuctionSessionResponse::from).toList();
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    public AuctionSession findById(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.SESSION_NOT_FOUND));
    }
}

