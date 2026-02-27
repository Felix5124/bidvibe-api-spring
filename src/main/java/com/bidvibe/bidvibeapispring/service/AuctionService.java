package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.AppConstants;
import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.auction.ApproveItemRequest;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionControlRequest;
import com.bidvibe.bidvibeapispring.dto.auction.AuctionResponse;
import com.bidvibe.bidvibeapispring.dto.ws.AuctionUpdatePayload;
import com.bidvibe.bidvibeapispring.entity.Auction;
import com.bidvibe.bidvibeapispring.entity.AuctionSession;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.repository.BidRepository;
import com.bidvibe.bidvibeapispring.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Xử lý nghiệp vụ cốt lõi của Đấu giá (Auction):
 * - Admin duyệt item + tạo auction
 * - Admin điều khiển phiên (Start/Pause/Stop/Cancel)
 * - Kết thúc phiên: xác định người thắng, chuyển tài sản, thanh toán
 * - Push WebSocket khi giá/trạng thái thay đổi
 * - Thông báo Watchlist khi phiên bắt đầu
 */
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final WatchlistRepository watchlistRepository;
    private final AuctionSessionService sessionService;
    private final ItemService itemService;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // ------------------------------------------------------------------
    // Admin – Approve item + create auction slot
    // ------------------------------------------------------------------

    /**
     * POST /api/admin/items/approve
     * Duyệt item, gán vào phiên, khởi tạo một Auction record.
     */
    @Transactional
    public AuctionResponse approveItemAndSchedule(ApproveItemRequest req) {
        AuctionSession session = sessionService.findById(req.getSessionId());
        Item item = itemService.approveAndMoveToAuction(req.getItemId(), req.getRarity());

        Instant endTime = req.getEndTime();
        // English: endTime tính từ khi START, admin không cần cung cấp trước
        // Dutch / Sealed: admin cung cấp endTime

        Auction auction = auctionRepository.save(Auction.builder()
                .session(session)
                .item(item)
                .startPrice(req.getStartPrice())
                .currentPrice(req.getStartPrice())
                .minPrice(req.getMinPrice())
                .stepPrice(req.getStepPrice())
                .decreaseAmount(req.getDecreaseAmount())
                .intervalSeconds(AppConstants.DUTCH_PRICE_DECREASE_INTERVAL_SECONDS)
                .endTime(endTime)
                .orderIndex(Objects.requireNonNullElse(req.getOrderIndex(), 0))
                .status(Auction.Status.WAITING)
                .build());

        return AuctionResponse.from(auction);
    }

    // ------------------------------------------------------------------
    // Admin – Control panel
    // ------------------------------------------------------------------

    /**
     * POST /api/admin/auctions/{auctionId}/control
     * Hỗ trợ: START, PAUSE, RESUME, STOP, CANCEL
     */
    @Transactional
    public AuctionResponse controlAuction(UUID auctionId, AuctionControlRequest req) {
        Auction auction = findById(auctionId);

        switch (req.getAction()) {
            case START   -> startAuction(auction);
            case PAUSE   -> pauseAuction(auction);
            case RESUME  -> resumeAuction(auction);
            case STOP    -> endAuction(auction);
            case CANCEL  -> cancelAuction(auction);
        }

        return AuctionResponse.from(auctionRepository.save(auction));
    }

    // ------------------------------------------------------------------
    // Auction lifecycle helpers
    // ------------------------------------------------------------------

    /** Kích hoạt auction, tính endTime cho English, thông báo Watchlist. */
    @Transactional
    public void startAuction(Auction auction) {
        if (auction.getStatus() != Auction.Status.WAITING) {
            throw new BidVibeException(ErrorCode.SESSION_ALREADY_STARTED);
        }
        auction.setStatus(Auction.Status.ACTIVE);

        // English auction: endTime = now + 2 phút
        if (auction.getSession().getType() == AuctionSession.Type.ENGLISH) {
            auction.setEndTime(
                    Instant.now().plusSeconds(AppConstants.ENGLISH_AUCTION_DURATION_SECONDS));
        }
        auctionRepository.save(auction);

        // Thông báo tất cả user đang theo dõi
        watchlistRepository.findByAuctionId(auction.getId())
                .forEach(w -> notificationService.sendNotification(
                        w.getUser(),
                        "Phiên đấu giá đã bắt đầu!",
                        "\"" + auction.getItem().getName() + "\" đang diễn ra. Tham gia ngay!",
                        com.bidvibe.bidvibeapispring.dto.ws.NotificationPayload.NotificationType.WATCHLIST_START,
                        auction.getId()));

        broadcastAuctionUpdate(auction);
    }

    private void pauseAuction(Auction auction) {
        if (auction.getStatus() != Auction.Status.ACTIVE) {
            throw new BidVibeException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        // Pause: tạm dừng bằng cách chuyển về WAITING (resume → ACTIVE lại)
        auction.setStatus(Auction.Status.WAITING);
    }

    private void resumeAuction(Auction auction) {
        if (auction.getStatus() != Auction.Status.WAITING) {
            throw new BidVibeException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        auction.setStatus(Auction.Status.ACTIVE);
        broadcastAuctionUpdate(auction);
    }

    /**
     * Kết thúc auction: xác định winner, chuyển item, thanh toán.
     * Gọi từ Admin hoặc Scheduler khi hết giờ.
     */
    @Transactional
    public void endAuction(Auction auction) {
        if (auction.getStatus() == Auction.Status.ENDED ||
            auction.getStatus() == Auction.Status.CANCELLED) {
            return; // idempotent
        }
        auction.setStatus(Auction.Status.ENDED);

        bidRepository.findHighestBidInAuction(auction.getId()).ifPresent(winningBid -> {
            User winner = winningBid.getUser();
            auction.setWinner(winner);
            auctionRepository.save(auction);

            // Tính phí sàn
            BigDecimal finalAmount = winningBid.getAmount();
            BigDecimal fee = finalAmount.multiply(AppConstants.PLATFORM_FEE_RATE);
            User seller = auction.getItem().getSeller();

            // Thanh toán
            walletService.processFinalPayment(winner.getId(), seller.getId(), finalAmount, fee);

            // Chuyển quyền sở hữu item + cooldown
            itemService.transferToWinner(auction.getItem().getId(), winner);

            // Thông báo người thắng
            notificationService.sendNotification(
                    winner,
                    "Chúc mừng! Bạn đã thắng thầu!",
                    "Bạn đã thắng phiên đấu giá \"" + auction.getItem().getName() + "\" với giá "
                            + finalAmount.toPlainString() + " VND.",
                    com.bidvibe.bidvibeapispring.dto.ws.NotificationPayload.NotificationType.WIN,
                    auction.getId());
        });

        broadcastAuctionUpdate(auction);
    }

    private void cancelAuction(Auction auction) {
        auction.setStatus(Auction.Status.CANCELLED);
        // Trả lại item về APPROVED để Admin sắp xếp lại
        auction.getItem().setStatus(Item.Status.APPROVED);
        broadcastAuctionUpdate(auction);
    }

    // ------------------------------------------------------------------
    // Popcorn Bidding – extend timer (gọi từ BidService)
    // ------------------------------------------------------------------

    /**
     * Nếu bid xảy ra trong 30 giây cuối → reset đồng hồ về 30 giây.
     */
    @Transactional
    public void applyPopcornBidding(Auction auction) {
        if (auction.getEndTime() == null) return;
        long secondsLeft = auction.getEndTime().getEpochSecond() - Instant.now().getEpochSecond();
        if (secondsLeft <= AppConstants.POPCORN_BIDDING_THRESHOLD_SECONDS) {
            auction.setEndTime(
                    Instant.now().plusSeconds(AppConstants.POPCORN_BIDDING_THRESHOLD_SECONDS));
            auctionRepository.save(auction);
        }
    }

    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AuctionResponse getAuction(UUID auctionId) {
        return AuctionResponse.from(findById(auctionId));
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAuctionsBySession(UUID sessionId) {
        return auctionRepository.findBySessionIdOrderByOrderIndex(sessionId)
                .stream().map(AuctionResponse::from).toList();
    }

    // ------------------------------------------------------------------
    // WebSocket broadcast
    // ------------------------------------------------------------------

    public void broadcastAuctionUpdate(Auction auction) {
        long totalBids = bidRepository.countByAuctionId(auction.getId());
        AuctionUpdatePayload payload = AuctionUpdatePayload.builder()
                .auctionId(auction.getId())
                .currentPrice(auction.getCurrentPrice())
                .status(auction.getStatus())
                .endTime(auction.getEndTime())
                .totalBids(totalBids)
                .currentLeader(auction.getWinner() != null
                        ? com.bidvibe.bidvibeapispring.dto.user.UserSummary.builder()
                                .id(auction.getWinner().getId())
                                .nickname(auction.getWinner().getNickname())
                                .avatarUrl(auction.getWinner().getAvatarUrl())
                                .reputationScore(auction.getWinner().getReputationScore())
                                .build()
                        : null)
                .build();

        messagingTemplate.convertAndSend("/topic/auction/" + auction.getId(), payload);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    public Auction findById(UUID auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.AUCTION_NOT_FOUND));
    }
}
