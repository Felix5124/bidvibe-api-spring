package com.bidvibe.bidvibeapispring.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidvibe.bidvibeapispring.constant.AppConstants;
import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.item.ItemResponse;
import com.bidvibe.bidvibeapispring.dto.item.ListItemOnMarketRequest;
import com.bidvibe.bidvibeapispring.dto.item.SubmitItemRequest;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.entity.MarketListing;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.ItemRepository;
import com.bidvibe.bidvibeapispring.repository.MarketListingRepository;

import lombok.RequiredArgsConstructor;

/**
 * Xử lý nghiệp vụ Vật phẩm:
 * - Ký gửi đồ (user submit)
 * - Kho đồ (inventory)
 * - Niêm yết trên Chợ Đen
 * - Tìm kiếm Chợ Đen
 * - Xác nhận nhận hàng (confirm receipt)
 */
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final MarketListingRepository marketListingRepository;
    private final UserService userService;

    // ------------------------------------------------------------------
    // User – Submit item (ký gửi)
    // ------------------------------------------------------------------

    /** POST /api/items/submit */
    @Transactional
    public ItemResponse submitItem(UUID userId, SubmitItemRequest req) {
        User seller = userService.findById(userId);
        Item item = Item.builder()
                .seller(seller)
                .currentOwner(seller)
                .name(req.getName())
                .description(req.getDescription())
                .imageUrls(req.getImageUrls())
                .rarity(req.getRarity() != null ? req.getRarity() : Item.Rarity.COMMON)
                .status(Item.Status.PENDING)
                .build();
        return ItemResponse.from(itemRepository.save(item));
    }

    // ------------------------------------------------------------------
    // User – Inventory (kho đồ)
    // ------------------------------------------------------------------

    /** GET /api/items/inventory */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getInventory(UUID userId, Pageable pageable) {
        return itemRepository
                .findByCurrentOwnerIdAndStatus(userId, Item.Status.IN_INVENTORY, pageable)
                .map(ItemResponse::from);
    }

    // ------------------------------------------------------------------
    // Black Market
    // ------------------------------------------------------------------

    /** GET /api/market/items – tìm kiếm Chợ Đen với keyword & rarity filter */
    @Transactional(readOnly = true)
    public Page<ItemResponse> searchBlackMarket(String keyword, Item.Rarity rarity, Pageable pageable) {
        return marketListingRepository.searchActive(keyword, rarity, pageable)
                .map(listing -> ItemResponse.from(listing.getItem()));
    }

    /** POST /api/market/list – niêm yết đồ từ kho lên Chợ Đen */
    @Transactional
    public ItemResponse listOnMarket(UUID userId, ListItemOnMarketRequest req) {
        Item item = findById(req.getItemId());
        validateOwner(item, userId);

        if (item.getStatus() != Item.Status.IN_INVENTORY) {
            throw new BidVibeException(ErrorCode.ITEM_NOT_IN_INVENTORY);
        }
        if (itemRepository.isInCooldown(item.getId())) {
            throw new BidVibeException(ErrorCode.ITEM_IN_COOLDOWN);
        }

        MarketListing listing = MarketListing.builder()
                .item(item)
                .seller(userService.findById(userId))
                .askingPrice(req.getAskingPrice())
                .build();
        marketListingRepository.save(listing);
        return ItemResponse.from(item);
    }

    /** POST /api/items/confirm-receipt – xác nhận nhận đồ thật, kết thúc vòng đời trên sàn */
    @Transactional
    public ItemResponse confirmReceipt(UUID userId, UUID itemId) {
        Item item = findById(itemId);
        validateOwner(item, userId);

        if (item.getStatus() != Item.Status.IN_INVENTORY) {
            throw new BidVibeException(ErrorCode.ITEM_NOT_AVAILABLE);
        }

        item.setStatus(Item.Status.SHIPPED);
        return ItemResponse.from(itemRepository.save(item));
    }

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    /** GET /api/admin/items/pending */
    @Transactional(readOnly = true)
    public java.util.List<ItemResponse> getPendingItems() {
        return itemRepository.findByStatus(Item.Status.PENDING)
                .stream().map(ItemResponse::from).toList();
    }

    /** GET /api/admin/items?status=&page= */
    @Transactional(readOnly = true)
    public Page<ItemResponse> adminListItems(Item.Status status, org.springframework.data.domain.Pageable pageable) {
        if (status != null) {
            return itemRepository.findByStatus(status, pageable).map(ItemResponse::from);
        }
        return itemRepository.findAll(pageable).map(ItemResponse::from);
    }

    /** GET /api/items/{id} (public), GET /api/admin/items/{id} */
    @Transactional(readOnly = true)
    public ItemResponse getItemDetail(UUID itemId) {
        return ItemResponse.from(findById(itemId));
    }

    /** POST /api/admin/items/{id}/reject – từ chối item kèm lý do. */
    @Transactional
    public ItemResponse rejectItem(UUID itemId, String reason) {
        Item item = findById(itemId);
        if (item.getStatus() != Item.Status.PENDING) {
            throw new BidVibeException(ErrorCode.ITEM_NOT_AVAILABLE);
        }
        item.setStatus(Item.Status.REJECTED);
        return ItemResponse.from(itemRepository.save(item));
    }

    /**
     * POST /api/admin/items/{id}/approve – duyệt item, gán tags + rarity, chuyển status → APPROVED.
     */
    @Transactional
    public ItemResponse approveItem(UUID itemId, Item.Rarity rarity, java.util.List<String> tags) {
        Item item = findById(itemId);
        if (item.getStatus() != Item.Status.PENDING) {
            throw new BidVibeException(ErrorCode.ITEM_NOT_AVAILABLE);
        }
        if (tags != null) item.setTags(tags);
        item.setRarity(rarity);
        item.setStatus(Item.Status.APPROVED);
        return ItemResponse.from(itemRepository.save(item));
    }

    /**
     * Được gọi từ AuctionService sau khi Admin duyệt.
     * Cập nhật rarity (Admin gán) và chuyển status → IN_AUCTION.
     */
    @Transactional
    public Item approveAndMoveToAuction(UUID itemId, Item.Rarity rarity) {
        Item item = findById(itemId);
        if (item.getStatus() != Item.Status.PENDING) {
            throw new BidVibeException(ErrorCode.ITEM_NOT_AVAILABLE);
        }
        item.setRarity(rarity);
        item.setStatus(Item.Status.IN_AUCTION);
        return itemRepository.save(item);
    }

    /**
     * Chuyển item về IN_INVENTORY + gán current owner sau khi thắng thầu.
     * Áp dụng cooldown 12h.
     */
    @Transactional
    public void transferToWinner(UUID itemId, User winner) {
        Item item = findById(itemId);
        item.setCurrentOwner(winner);
        item.setStatus(Item.Status.IN_INVENTORY);
        item.setCooldownUntil(Instant.now().plusSeconds(AppConstants.ITEM_COOLDOWN_SECONDS));
        itemRepository.save(item);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    public Item findById(UUID itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.ITEM_NOT_FOUND));
    }

    private void validateOwner(Item item, UUID userId) {
        if (item.getCurrentOwner() == null || !item.getCurrentOwner().getId().equals(userId)) {
            throw new BidVibeException(ErrorCode.ITEM_NOT_OWNED);
        }
    }
}
