package com.bidvibe.bidvibeapispring.constant;

import java.math.BigDecimal;

/**
 * Hằng số nghiệp vụ toàn cục của BidVibe.
 * Tập trung mọi "magic number" tại đây để dễ điều chỉnh.
 */
public final class AppConstants {

    private AppConstants() {}

    // -------------------------------------------------------------------------
    // AUCTION – English Auction
    // -------------------------------------------------------------------------

    /** Thời gian mỗi món đấu giá (English): 2 phút = 120 giây. */
    public static final int ENGLISH_AUCTION_DURATION_SECONDS = 120;

    /**
     * Popcorn Bidding: nếu có bid trong 30 giây cuối,
     * đồng hồ reset về 30 giây.
     */
    public static final int POPCORN_BIDDING_THRESHOLD_SECONDS = 30;

    /** Thời gian nghỉ giữa hai món trong cùng phiên: 15 giây. */
    public static final int BREAK_BETWEEN_ITEMS_SECONDS = 15;

    // -------------------------------------------------------------------------
    // AUCTION – Dutch Auction
    // -------------------------------------------------------------------------

    /** Khoảng thời gian giảm giá mỗi nấc cho Dutch Auction (giây). */
    public static final int DUTCH_PRICE_DECREASE_INTERVAL_SECONDS = 5;

    // -------------------------------------------------------------------------
    // AUCTION – Sealed-bid Auction
    // -------------------------------------------------------------------------

    /** Thời gian mở cửa đặt giá kín: 24 giờ (tính bằng giây). */
    public static final long SEALED_BID_DURATION_SECONDS = 24 * 60 * 60L;

    // -------------------------------------------------------------------------
    // ITEM – Cooldown & Lifecycle
    // -------------------------------------------------------------------------

    /** Thời gian khóa chuyển nhượng sau mỗi giao dịch: 12 giờ (tính bằng giây). */
    public static final long ITEM_COOLDOWN_SECONDS = 12 * 60 * 60L;

    // -------------------------------------------------------------------------
    // WALLET & FEE
    // -------------------------------------------------------------------------

    /**
     * Phí sàn tính trên giá thắng thầu (5%).
     * Dùng: finalAmount = winPrice * (1 + PLATFORM_FEE_RATE)
     */
    public static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05");

    /** Số dư tối thiểu phải có trong ví để bắt đầu đặt giá. */
    public static final BigDecimal MIN_WALLET_BALANCE_TO_BID = BigDecimal.ZERO;

    // -------------------------------------------------------------------------
    // REPUTATION
    // -------------------------------------------------------------------------

    /** Điểm uy tín mặc định khi tạo tài khoản mới. */
    public static final BigDecimal DEFAULT_REPUTATION_SCORE = new BigDecimal("5.0");

    /** Số sao đánh giá tối thiểu. */
    public static final int MIN_RATING_STARS = 1;

    /** Số sao đánh giá tối đa. */
    public static final int MAX_RATING_STARS = 5;

    // -------------------------------------------------------------------------
    // PAGINATION
    // -------------------------------------------------------------------------

    /** Số phần tử mặc định trên một trang. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** Số phần tử tối đa cho phép trên một trang. */
    public static final int MAX_PAGE_SIZE = 100;

    // -------------------------------------------------------------------------
    // WEBSOCKET – Topic & Destination
    // -------------------------------------------------------------------------

    /** Prefix cho tất cả message broker topic. */
    public static final String WS_TOPIC_PREFIX = "/topic";

    /** Topic nhận cập nhật giá & người dẫn đầu của một auction. */
    public static final String WS_TOPIC_AUCTION_UPDATE = "/topic/auction/{auctionId}";

    /** Topic nhận tin nhắn chat live trong phòng đấu giá. */
    public static final String WS_TOPIC_CHAT = "/topic/chat/{auctionId}";

    /** Topic nhận đồng bộ đồng hồ đếm ngược. */
    public static final String WS_TOPIC_TIMER = "/topic/timer/{auctionId}";

    /** Topic nhận thông báo cá nhân (outbid, thắng thầu…). */
    public static final String WS_TOPIC_NOTIFICATION = "/topic/notification/{userId}";

    /** Prefix cho các endpoint gửi message từ client lên server. */
    public static final String WS_APP_PREFIX = "/app";

    // -------------------------------------------------------------------------
    // API BASE PATHS
    // -------------------------------------------------------------------------

    public static final String API_USERS        = "/api/users";
    public static final String API_WALLET       = "/api/wallet";
    public static final String API_ITEMS        = "/api/items";
    public static final String API_AUCTIONS     = "/api/auctions";
    public static final String API_MARKET       = "/api/market";
    public static final String API_ANALYTICS    = "/api/analytics";
    public static final String API_ADMIN        = "/api/admin";
    public static final String API_NOTIFICATIONS = "/api/notifications";
}

