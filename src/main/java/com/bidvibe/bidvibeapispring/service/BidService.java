package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.bid.*;
import com.bidvibe.bidvibeapispring.dto.ws.NotificationPayload;
import com.bidvibe.bidvibeapispring.entity.*;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.AuctionRepository;
import com.bidvibe.bidvibeapispring.repository.BidRepository;
import com.bidvibe.bidvibeapispring.repository.ProxyBidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Engine đặt giá trung tâm – xử lý 3 hình thức đấu giá:
 *
 * <ol>
 *   <li><b>English Auction</b>: giá tăng dần, Popcorn Bidding, Proxy Bidding tự động.</li>
 *   <li><b>Dutch Auction</b>: giá giảm dần theo lịch, người đầu tiên Buy-Now thắng.</li>
 *   <li><b>Sealed-bid Auction</b>: đặt giá bí mật trong 24h, mở thầu cuối kỳ.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ProxyBidRepository proxyBidRepository;
    private final WalletService walletService;
    private final AuctionService auctionService;
    private final NotificationService notificationService;
    private final UserService userService;

    // ======================================================================
    // 1. ENGLISH AUCTION – Manual Bid
    // ======================================================================

    /**
     * POST /api/auctions/bid
     * Đặt giá thủ công trong English Auction.
     * Luồng:
     * 1. Validate auction ACTIVE + type ENGLISH
     * 2. Validate giá > currentPrice + stepPrice
     * 3. Lock tiền trong ví; unlock tiền người bid trước
     * 4. Lưu Bid, cập nhật currentPrice
     * 5. Popcorn Bidding (reset timer nếu < 30s)
     * 6. Kích hoạt Proxy Bidding engine
     * 7. Push WebSocket + thông báo người bị outbid
     */
    @Transactional
    public BidResponse placeBid(UUID userId, PlaceBidRequest req) {
        Auction auction = auctionService.findById(req.getAuctionId());
        validateEnglishAuction(auction, userId);

        BigDecimal minRequired = calcMinBid(auction);
        if (req.getAmount().compareTo(minRequired) < 0) {
            throw new BidVibeException(ErrorCode.AUCTION_BID_TOO_LOW,
                    "Giá tối thiểu phải đặt là " + minRequired.toPlainString() + " VND");
        }

        User bidder = userService.findById(userId);

        // Unlock tiền người bid cao nhất trước đó (nếu khác người)
        bidRepository.findHighestBidInAuction(auction.getId())
                .ifPresent(prev -> {
                    if (!prev.getUser().getId().equals(userId)) {
                        walletService.unlockFunds(prev.getUser().getId(), prev.getAmount());
                        notifyOutbid(prev.getUser(), auction);
                    }
                });

        // Lock tiền người bid mới
        walletService.lockFunds(userId, req.getAmount());

        // Lưu bid
        Bid bid = saveBid(auction, bidder, req.getAmount(), false);

        // Cập nhật currentPrice và winner tạm thời
        auction.setCurrentPrice(req.getAmount());
        auction.setWinner(bidder);
        auctionRepository.save(auction);

        // Popcorn Bidding
        auctionService.applyPopcornBidding(auction);

        // Proxy Bidding engine (phản ứng tự động với bid thủ công)
        runProxyEngine(auction, userId, req.getAmount());

        // Broadcast
        auctionService.broadcastAuctionUpdate(auction);

        return BidResponse.from(bid);
    }

    // ======================================================================
    // 2. DUTCH AUCTION – Buy Now
    // ======================================================================

    /**
     * POST /api/auctions/buy-now
     * Người đầu tiên bấm "MUA" ở giá hiện tại thắng ngay.
     */
    @Transactional
    public BidResponse buyNow(UUID userId, BuyNowRequest req) {
        Auction auction = auctionService.findById(req.getAuctionId());
        validateActiveAuction(auction);

        if (auction.getSession().getType() != AuctionSession.Type.DUTCH) {
            throw new BidVibeException(ErrorCode.AUCTION_NOT_ACTIVE,
                    "Buy-now chỉ áp dụng cho Dutch Auction");
        }

        // Không được mua đồ của chính mình
        if (auction.getItem().getSeller().getId().equals(userId)) {
            throw new BidVibeException(ErrorCode.AUCTION_BID_ON_OWN_ITEM);
        }

        BigDecimal buyPrice = auction.getCurrentPrice();
        User buyer = userService.findById(userId);

        walletService.lockFunds(userId, buyPrice);

        Bid bid = saveBid(auction, buyer, buyPrice, false);

        auction.setWinner(buyer);
        auction.setCurrentPrice(buyPrice);
        auctionRepository.save(auction);

        // Kết thúc ngay
        auctionService.endAuction(auction);

        return BidResponse.from(bid);
    }

    // ======================================================================
    // 3. SEALED-BID AUCTION
    // ======================================================================

    /**
     * POST /api/auctions/sealed-submit
     * Gửi giá kín – mỗi user chỉ được gửi 1 lần; giá ẩn cho đến khi endTime.
     */
    @Transactional
    public BidResponse submitSealedBid(UUID userId, SealedBidRequest req) {
        Auction auction = auctionService.findById(req.getAuctionId());
        validateActiveAuction(auction);

        if (auction.getSession().getType() != AuctionSession.Type.SEALED) {
            throw new BidVibeException(ErrorCode.AUCTION_NOT_ACTIVE,
                    "Sealed-bid chỉ áp dụng cho Sealed Auction");
        }
        if (auction.getItem().getSeller().getId().equals(userId)) {
            throw new BidVibeException(ErrorCode.AUCTION_BID_ON_OWN_ITEM);
        }
        if (bidRepository.existsByAuctionIdAndUserId(req.getAuctionId(), userId)) {
            throw new BidVibeException(ErrorCode.AUCTION_SEALED_DUPLICATE);
        }
        // Giá phải >= startPrice (giá sàn Sealed)
        if (req.getAmount().compareTo(auction.getStartPrice()) < 0) {
            throw new BidVibeException(ErrorCode.AUCTION_BID_TOO_LOW,
                    "Giá kín tối thiểu là " + auction.getStartPrice().toPlainString() + " VND");
        }

        User bidder = userService.findById(userId);
        walletService.lockFunds(userId, req.getAmount());

        Bid bid = saveBid(auction, bidder, req.getAmount(), false);
        return BidResponse.from(bid);
    }

    /**
     * Mở thầu Sealed – gọi bởi Scheduler khi đến endTime.
     * Xác định người bid cao nhất; unlock tiền những người thua.
     */
    @Transactional
    public void openSealedBids(UUID auctionId) {
        Auction auction = auctionService.findById(auctionId);

        List<Bid> allBids = bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId);
        if (allBids.isEmpty()) {
            auctionService.endAuction(auction);
            return;
        }

        // Tìm bid cao nhất
        Bid winningBid = allBids.stream()
                .max(Comparator.comparing(Bid::getAmount))
                .orElseThrow();

        // Unlock tiền những người thua
        allBids.stream()
                .filter(b -> !b.getId().equals(winningBid.getId()))
                .forEach(b -> walletService.unlockFunds(b.getUser().getId(), b.getAmount()));

        // Cập nhật auction
        auction.setCurrentPrice(winningBid.getAmount());
        auction.setWinner(winningBid.getUser());
        auctionRepository.save(auction);

        // Kết thúc auction (xử lý thanh toán + chuyển item)
        auctionService.endAuction(auction);
    }

    // ======================================================================
    // 4. PROXY BID – Set / Cancel
    // ======================================================================

    /**
     * Đặt hoặc cập nhật hạn mức Proxy Bid.
     * Nếu đã có → cập nhật maxAmount; chưa có → tạo mới.
     */
    @Transactional
    public ProxyBidResponse setProxyBid(UUID userId, SetProxyBidRequest req) {
        Auction auction = auctionService.findById(req.getAuctionId());
        validateEnglishAuction(auction, userId);

        ProxyBid proxyBid = proxyBidRepository
                .findByAuctionIdAndUserIdAndIsActiveTrue(req.getAuctionId(), userId)
                .map(existing -> {
                    existing.setMaxAmount(req.getMaxAmount());
                    return existing;
                })
                .orElseGet(() -> ProxyBid.builder()
                        .auction(auction)
                        .user(userService.findById(userId))
                        .maxAmount(req.getMaxAmount())
                        .isActive(true)
                        .build());

        return ProxyBidResponse.from(proxyBidRepository.save(proxyBid));
    }

    /** Huỷ Proxy Bid (đặt isActive = false). */
    @Transactional
    public void cancelProxyBid(UUID userId, UUID auctionId) {
        ProxyBid proxyBid = proxyBidRepository
                .findByAuctionIdAndUserIdAndIsActiveTrue(auctionId, userId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.PROXY_BID_NOT_FOUND));
        proxyBid.setActive(false);
        proxyBidRepository.save(proxyBid);
    }

    // ======================================================================
    // Proxy Bidding Engine (internal)
    // ======================================================================

    /**
     * Sau mỗi manual bid, kiểm tra proxy bids còn lại.
     * Nếu có proxy bidder khác, tự động nâng giá hộ họ đến maxAmount.
     *
     * @param triggerUserId userId vừa đặt bid thủ công (không nâng proxy của người này)
     * @param newPrice      giá vừa được đặt
     */
    private void runProxyEngine(Auction auction, UUID triggerUserId, BigDecimal newPrice) {
        List<ProxyBid> activeProxies = proxyBidRepository
                .findByAuctionIdAndIsActiveTrue(auction.getId());

        // Tìm proxy bid có maxAmount cao nhất (không phải người vừa bid)
        activeProxies.stream()
                .filter(p -> !p.getUser().getId().equals(triggerUserId))
                .filter(p -> p.getMaxAmount().compareTo(newPrice) > 0)
                .max(Comparator.comparing(ProxyBid::getMaxAmount))
                .ifPresent(proxy -> {
                    BigDecimal proxyAmount = newPrice.add(
                            auction.getStepPrice() != null
                                    ? auction.getStepPrice()
                                    : BigDecimal.ONE);

                    // Không vượt maxAmount của proxy
                    if (proxyAmount.compareTo(proxy.getMaxAmount()) > 0) {
                        proxyAmount = proxy.getMaxAmount();
                    }
                    if (proxyAmount.compareTo(newPrice) <= 0) return;

                    // Unlock tiền người bid thủ công vừa rồi (bị proxy vượt ngay)
                    walletService.unlockFunds(triggerUserId, newPrice);
                    notifyOutbid(userService.findById(triggerUserId), auction);

                    // Lock tiền proxy bidder
                    walletService.lockFunds(proxy.getUser().getId(), proxyAmount);

                    saveBid(auction, proxy.getUser(), proxyAmount, true);

                    auction.setCurrentPrice(proxyAmount);
                    auction.setWinner(proxy.getUser());
                    auctionRepository.save(auction);

                    // Nếu đã dùng hết maxAmount → vô hiệu hóa proxy
                    if (proxyAmount.compareTo(proxy.getMaxAmount()) == 0) {
                        proxy.setActive(false);
                        proxyBidRepository.save(proxy);
                    }

                    auctionService.applyPopcornBidding(auction);
                    auctionService.broadcastAuctionUpdate(auction);
                });
    }

    // ======================================================================
    // Validation helpers
    // ======================================================================

    private void validateEnglishAuction(Auction auction, UUID userId) {
        validateActiveAuction(auction);
        if (auction.getSession().getType() != AuctionSession.Type.ENGLISH) {
            throw new BidVibeException(ErrorCode.AUCTION_NOT_ACTIVE,
                    "Hình thức này chỉ áp dụng cho English Auction");
        }
        if (auction.getItem().getSeller().getId().equals(userId)) {
            throw new BidVibeException(ErrorCode.AUCTION_BID_ON_OWN_ITEM);
        }
    }

    private void validateActiveAuction(Auction auction) {
        if (auction.getStatus() == Auction.Status.ENDED ||
            auction.getStatus() == Auction.Status.CANCELLED) {
            throw new BidVibeException(ErrorCode.AUCTION_ALREADY_ENDED);
        }
        if (auction.getStatus() != Auction.Status.ACTIVE) {
            throw new BidVibeException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        if (auction.getEndTime() != null && auction.getEndTime().isBefore(java.time.Instant.now())) {
            throw new BidVibeException(ErrorCode.AUCTION_ALREADY_ENDED);
        }
    }

    private BigDecimal calcMinBid(Auction auction) {
        BigDecimal step = auction.getStepPrice() != null
                ? auction.getStepPrice() : BigDecimal.ZERO;
        BigDecimal current = auction.getCurrentPrice() != null
                ? auction.getCurrentPrice() : auction.getStartPrice();
        return current.add(step);
    }

    // ======================================================================
    // Persistence helpers
    // ======================================================================

    private Bid saveBid(Auction auction, User user, BigDecimal amount, boolean isProxy) {
        return bidRepository.save(Bid.builder()
                .auction(auction)
                .user(user)
                .amount(amount)
                .isProxy(isProxy)
                .build());
    }

    private void notifyOutbid(User user, Auction auction) {
        notificationService.sendNotification(
                user,
                "Bạn vừa bị vượt giá!",
                "Có người đã đặt giá cao hơn bạn trong phiên đấu giá \""
                        + auction.getItem().getName() + "\". Hãy trả giá lại!",
                NotificationPayload.NotificationType.OUTBID,
                auction.getId());
    }
}
