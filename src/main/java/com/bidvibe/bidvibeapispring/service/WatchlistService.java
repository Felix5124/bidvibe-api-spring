package com.bidvibe.bidvibeapispring.service;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.item.ItemResponse;
import com.bidvibe.bidvibeapispring.entity.Item;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.entity.Watchlist;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.ItemRepository;
import com.bidvibe.bidvibeapispring.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Xử lý nghiệp vụ Watchlist:
 * - Thêm / Xóa theo dõi (toggle) theo item
 * - Lấy danh sách item đang theo dõi
 */
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    // ------------------------------------------------------------------
    // Toggle
    // ------------------------------------------------------------------

    /**
     * POST /api/users/watchlist/{itemId} – toggle theo dõi.
     * Nếu chưa theo dõi → thêm, đã theo dõi → xóa.
     * @return true nếu đã thêm, false nếu đã xóa
     */
    @Transactional
    public boolean toggleWatchlist(UUID userId, UUID itemId) {
        if (watchlistRepository.existsByUserIdAndItemId(userId, itemId)) {
            watchlistRepository.deleteByUserIdAndItemId(userId, itemId);
            return false;
        }

        User user = userService.findById(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.ITEM_NOT_FOUND));

        watchlistRepository.save(Watchlist.builder()
                .user(user)
                .item(item)
                .build());
        return true;
    }

    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    /** GET /api/users/me/watchlist */
    @Transactional(readOnly = true)
    public List<Watchlist> getWatchlist(UUID userId) {
        return watchlistRepository.findByUserId(userId);
    }

    /** GET /api/users/me/watchlist – items phân trang. */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getWatchlistItems(UUID userId, Pageable pageable) {
        List<ItemResponse> items = watchlistRepository.findByUserId(userId).stream()
                .map(w -> ItemResponse.from(w.getItem()))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<>(start <= items.size() ? items.subList(start, end) : List.of(),
                pageable, items.size());
    }

    /** DELETE /api/users/me/watchlist/{itemId} – xóa khỏi danh sách theo dõi. */
    @Transactional
    public void removeFromWatchlist(UUID userId, UUID itemId) {
        watchlistRepository.deleteByUserIdAndItemId(userId, itemId);
    }
}
