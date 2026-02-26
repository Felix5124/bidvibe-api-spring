package com.bidvibe.bidvibeapispring.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Mã lỗi chuẩn hóa cho toàn bộ BidVibe API.
 * Mỗi ErrorCode gắn với một HTTP status và message mặc định.
 * Dùng trong GlobalExceptionHandler và các custom exception.
 *
 * Quy tắc đặt tên mã: [DOMAIN]_[VẤNĐỀ]
 */
@Getter
public enum ErrorCode {

    // -------------------------------------------------------------------------
    // COMMON
    // -------------------------------------------------------------------------
    VALIDATION_FAILED       (400, HttpStatus.BAD_REQUEST,           "Dữ liệu đầu vào không hợp lệ."),
    RESOURCE_NOT_FOUND      (404, HttpStatus.NOT_FOUND,             "Không tìm thấy tài nguyên yêu cầu."),
    ACCESS_DENIED           (403, HttpStatus.FORBIDDEN,             "Bạn không có quyền thực hiện hành động này."),
    UNAUTHORIZED            (401, HttpStatus.UNAUTHORIZED,          "Xác thực thất bại. Vui lòng đăng nhập lại."),
    INTERNAL_SERVER_ERROR   (500, HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống. Vui lòng thử lại sau."),

    // -------------------------------------------------------------------------
    // USER
    // -------------------------------------------------------------------------
    USER_NOT_FOUND          (1001, HttpStatus.NOT_FOUND,     "Không tìm thấy người dùng."),
    USER_EMAIL_DUPLICATED   (1002, HttpStatus.CONFLICT,      "Email đã được đăng ký."),
    USER_BANNED             (1003, HttpStatus.FORBIDDEN,     "Tài khoản của bạn đã bị khóa vĩnh viễn."),

    // -------------------------------------------------------------------------
    // WALLET
    // -------------------------------------------------------------------------
    WALLET_NOT_FOUND        (2001, HttpStatus.NOT_FOUND,     "Không tìm thấy ví tiền."),
    WALLET_INSUFFICIENT     (2002, HttpStatus.BAD_REQUEST,   "Số dư khả dụng không đủ để thực hiện giao dịch."),
    WALLET_OPTIMISTIC_LOCK  (2003, HttpStatus.CONFLICT,      "Giao dịch bị xung đột. Vui lòng thử lại."),

    // -------------------------------------------------------------------------
    // ITEM
    // -------------------------------------------------------------------------
    ITEM_NOT_FOUND          (3001, HttpStatus.NOT_FOUND,     "Không tìm thấy vật phẩm."),
    ITEM_NOT_OWNED          (3002, HttpStatus.FORBIDDEN,     "Bạn không sở hữu vật phẩm này."),
    ITEM_IN_COOLDOWN        (3003, HttpStatus.BAD_REQUEST,   "Vật phẩm đang trong thời gian khóa chuyển nhượng (12h)."),
    ITEM_NOT_AVAILABLE      (3004, HttpStatus.BAD_REQUEST,   "Vật phẩm không ở trạng thái phù hợp để thực hiện hành động này."),
    ITEM_NOT_IN_INVENTORY   (3005, HttpStatus.BAD_REQUEST,   "Vật phẩm không có trong kho của bạn."),

    // -------------------------------------------------------------------------
    // AUCTION SESSION
    // -------------------------------------------------------------------------
    SESSION_NOT_FOUND       (4001, HttpStatus.NOT_FOUND,     "Không tìm thấy phiên đấu giá."),
    SESSION_NOT_ACTIVE      (4002, HttpStatus.BAD_REQUEST,   "Phiên đấu giá không đang hoạt động."),
    SESSION_ALREADY_STARTED (4003, HttpStatus.CONFLICT,      "Phiên đấu giá đã được bắt đầu."),

    // -------------------------------------------------------------------------
    // AUCTION
    // -------------------------------------------------------------------------
    AUCTION_NOT_FOUND       (5001, HttpStatus.NOT_FOUND,     "Không tìm thấy phiên đấu giá chi tiết."),
    AUCTION_NOT_ACTIVE      (5002, HttpStatus.BAD_REQUEST,   "Lượt đấu giá này không đang diễn ra."),
    AUCTION_ALREADY_ENDED   (5003, HttpStatus.BAD_REQUEST,   "Lượt đấu giá đã kết thúc."),
    AUCTION_BID_TOO_LOW     (5004, HttpStatus.BAD_REQUEST,   "Giá đặt phải cao hơn giá hiện tại và bước giá tối thiểu."),
    AUCTION_BID_ON_OWN_ITEM (5005, HttpStatus.BAD_REQUEST,   "Bạn không thể đấu giá trên vật phẩm của chính mình."),
    AUCTION_SEALED_DUPLICATE(5006, HttpStatus.CONFLICT,      "Bạn đã đặt giá kín cho lượt đấu giá này rồi."),

    // -------------------------------------------------------------------------
    // PROXY BID
    // -------------------------------------------------------------------------
    PROXY_BID_NOT_FOUND     (6001, HttpStatus.NOT_FOUND,     "Không tìm thấy cấu hình đấu giá tự động."),
    PROXY_BID_ALREADY_SET   (6002, HttpStatus.CONFLICT,      "Bạn đã cài đặt đấu giá tự động cho lượt này. Hãy cập nhật thay vì tạo mới."),

    // -------------------------------------------------------------------------
    // WATCHLIST
    // -------------------------------------------------------------------------
    WATCHLIST_ALREADY_EXISTS(7001, HttpStatus.CONFLICT,      "Bạn đã theo dõi vật phẩm này rồi."),
    WATCHLIST_NOT_FOUND     (7002, HttpStatus.NOT_FOUND,     "Không tìm thấy mục theo dõi."),

    // -------------------------------------------------------------------------
    // RATING
    // -------------------------------------------------------------------------
    RATING_ALREADY_SUBMITTED(8001, HttpStatus.CONFLICT,      "Bạn đã đánh giá giao dịch này rồi."),
    RATING_NOT_ELIGIBLE     (8002, HttpStatus.FORBIDDEN,     "Bạn không đủ điều kiện để đánh giá giao dịch này."),
    RATING_INVALID_STARS    (8003, HttpStatus.BAD_REQUEST,   "Số sao đánh giá phải từ 1 đến 5."),

    // -------------------------------------------------------------------------
    // TRANSACTION
    // -------------------------------------------------------------------------
    TRANSACTION_NOT_FOUND   (9001, HttpStatus.NOT_FOUND,     "Không tìm thấy giao dịch."),
    TRANSACTION_ALREADY_PROCESSED(9002, HttpStatus.CONFLICT, "Giao dịch này đã được xử lý trước đó."),

    // -------------------------------------------------------------------------
    // MARKET (Black Market)
    // -------------------------------------------------------------------------
    MARKET_ITEM_NOT_FOR_SALE(10001, HttpStatus.BAD_REQUEST,  "Vật phẩm này chưa được niêm yết trên Chợ Đen."),
    MARKET_CANNOT_BUY_OWN   (10002, HttpStatus.BAD_REQUEST,  "Bạn không thể mua vật phẩm của chính mình."),

    // -------------------------------------------------------------------------
    // NOTIFICATION
    // -------------------------------------------------------------------------
    NOTIFICATION_NOT_FOUND  (11001, HttpStatus.NOT_FOUND,    "Không tìm thấy thông báo.");

    // -------------------------------------------------------------------------

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}

