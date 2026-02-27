package com.bidvibe.bidvibeapispring.exception;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;

import lombok.Getter;

/**
 * Exception chuẩn của BidVibe.
 * Mọi lỗi nghiệp vụ đều ném exception này với ErrorCode tương ứng.
 * GlobalExceptionHandler sẽ bắt và chuyển thành HTTP response phù hợp.
 */
@Getter
public class BidVibeException extends RuntimeException {

    private final ErrorCode errorCode;

    public BidVibeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BidVibeException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public BidVibeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
