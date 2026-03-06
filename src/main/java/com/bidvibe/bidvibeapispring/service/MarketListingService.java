package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.AppConstants;
import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.market.MarketListingResponse;
import com.bidvibe.bidvibeapispring.dto.message.MessageResponse;
import com.bidvibe.bidvibeapispring.dto.ws.NotificationPayload;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.entity.MarketListing;
import com.bidvibe.bidvibeapispring.entity.Message;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.MarketListingRepository;
import com.bidvibe.bidvibeapispring.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarketListingService {

    private final MarketListingRepository marketListingRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final ItemService itemService;

    // ------------------------------------------------------------------
    // Public – browse
    // ------------------------------------------------------------------

    /** GET /api/market – tìm kiếm listing đang ACTIVE */
    @Transactional(readOnly = true)
    public Page<MarketListingResponse> searchListings(String keyword, Item.Rarity rarity, Pageable pageable) {
        return marketListingRepository.searchActive(keyword, rarity, pageable)
                .map(MarketListingResponse::from);
    }

    /** GET /api/market/{id} – chi tiết một listing */
    @Transactional(readOnly = true)
    public MarketListingResponse getListingDetail(UUID listingId) {
        return MarketListingResponse.from(findById(listingId));
    }

    // ------------------------------------------------------------------
    // User actions
    // ------------------------------------------------------------------

    /** DELETE /api/market/{id} – chủ listing huỷ niêm yết */
    @Transactional
    public void cancelListing(UUID userId, UUID listingId) {
        MarketListing listing = findById(listingId);
        if (!listing.getSeller().getId().equals(userId)) {
            throw new BidVibeException(ErrorCode.ACCESS_DENIED);
        }
        if (listing.getStatus() != MarketListing.Status.ACTIVE) {
            throw new BidVibeException(ErrorCode.MARKET_ITEM_NOT_FOR_SALE);
        }
        listing.setStatus(MarketListing.Status.CANCELLED);
        marketListingRepository.save(listing);
    }

    /**
     * POST /api/market/{id}/buy – mua ngay với giá niêm yết.
     * Trình tự: lock tiền → thanh toán → chuyển quyền sở hữu item → cập nhật listing.
     */
    @Transactional
    public MarketListingResponse buyListing(UUID buyerId, UUID listingId) {
        MarketListing listing = findById(listingId);

        if (listing.getStatus() != MarketListing.Status.ACTIVE) {
            throw new BidVibeException(ErrorCode.MARKET_ITEM_NOT_FOR_SALE);
        }
        if (listing.getSeller().getId().equals(buyerId)) {
            throw new BidVibeException(ErrorCode.MARKET_CANNOT_BUY_OWN);
        }

        BigDecimal askingPrice = listing.getAskingPrice();
        BigDecimal fee = askingPrice.multiply(AppConstants.PLATFORM_FEE_RATE);

        // Lock funds rồi process payment (giống flow đấu giá)
        walletService.lockFunds(buyerId, askingPrice);
        walletService.processFinalPayment(buyerId, listing.getSeller().getId(), askingPrice, fee);

        // Chuyển quyền sở hữu item
        User buyer = userService.findById(buyerId);
        itemService.transferToWinner(listing.getItem().getId(), buyer);

        // Cập nhật listing
        listing.setStatus(MarketListing.Status.SOLD);
        listing.setBuyer(buyer);
        MarketListing saved = marketListingRepository.save(listing);

        // Thông báo người bán
        notificationService.sendNotification(
                listing.getSeller(),
                "Đồ của bạn đã được mua!",
                "\"" + listing.getItem().getName() + "\" đã được mua với giá "
                        + askingPrice.toPlainString() + " VND.",
                NotificationPayload.NotificationType.SYSTEM,
                listingId);

        return MarketListingResponse.from(saved);
    }

    // ------------------------------------------------------------------
    // Listing messages (thương lượng P2P)
    // ------------------------------------------------------------------

    /** GET /api/market/{id}/messages – lịch sử tin nhắn của listing */
    @Transactional(readOnly = true)
    public List<MessageResponse> getListingMessages(UUID userId, UUID listingId) {
        MarketListing listing = findById(listingId);
        validateParticipant(listing, userId);
        return messageRepository.findByMarketListingIdOrderByCreatedAtAsc(listingId)
                .stream().map(MessageResponse::from).toList();
    }

    /** POST /api/market/{id}/messages – gửi tin nhắn thương lượng */
    @Transactional
    public MessageResponse sendListingMessage(UUID senderId, UUID listingId, String content) {
        MarketListing listing = findById(listingId);
        if (listing.getStatus() != MarketListing.Status.ACTIVE) {
            throw new BidVibeException(ErrorCode.MARKET_ITEM_NOT_FOR_SALE);
        }
        validateParticipant(listing, senderId);

        User sender = userService.findById(senderId);
        // Người nhận là phía còn lại
        User receiver = listing.getSeller().getId().equals(senderId)
                ? listing.getBuyer()    // seller nhắn — nhưng chưa có buyer, sẽ là null
                : listing.getSeller();

        Message message = messageRepository.save(Message.builder()
                .sender(sender)
                .receiver(receiver)
                .marketListing(listing)
                .content(content)
                .build());

        return MessageResponse.from(message);
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    public MarketListing findById(UUID listingId) {
        return marketListingRepository.findById(listingId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.MARKET_LISTING_NOT_FOUND));
    }

    /** Chỉ seller hoặc buyer được xem / gửi tin nhắn trong listing. */
    private void validateParticipant(MarketListing listing, UUID userId) {
        boolean isSeller = listing.getSeller().getId().equals(userId);
        boolean isBuyer = listing.getBuyer() != null && listing.getBuyer().getId().equals(userId);
        if (!isSeller && !isBuyer) {
            throw new BidVibeException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * POST /api/market/listings – niêm yết item từ kho lên Chợ Đen.
     * Validate: owner, IN_INVENTORY, no cooldown, no active listing.
     */
    @Transactional
    public MarketListingResponse createListing(UUID userId, UUID itemId, java.math.BigDecimal askingPrice) {
        com.bidvibe.bidvibeapispring.entity.Item item = itemService.findById(itemId);
        User seller = userService.findById(userId);

        if (!item.getCurrentOwner().getId().equals(userId)) {
            throw new BidVibeException(ErrorCode.ITEM_NOT_OWNED);
        }
        if (item.getStatus() != com.bidvibe.bidvibeapispring.entity.Item.Status.IN_INVENTORY) {
            throw new BidVibeException(ErrorCode.ITEM_NOT_IN_INVENTORY);
        }
        if (item.getCooldownUntil() != null && item.getCooldownUntil().isAfter(java.time.Instant.now())) {
            throw new BidVibeException(ErrorCode.ITEM_IN_COOLDOWN);
        }
        if (marketListingRepository.existsByItemIdAndStatus(itemId, MarketListing.Status.ACTIVE)) {
            throw new BidVibeException(ErrorCode.MARKET_LISTING_ALREADY_ACTIVE);
        }

        MarketListing listing = MarketListing.builder()
                .item(item)
                .seller(seller)
                .askingPrice(askingPrice)
                .build();
        return MarketListingResponse.from(marketListingRepository.save(listing));
    }

    /**
     * GET /api/admin/market/listings/{id}/messages – Admin xem toàn bộ chat P2P để giải quyết tranh chấp.
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> adminGetListingMessages(UUID listingId) {
        findById(listingId); // validate exists
        return messageRepository.findByMarketListingIdOrderByCreatedAtAsc(listingId)
                .stream().map(MessageResponse::from).toList();
    }
}
