package com.bidvibe.bidvibeapispring.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

/**
 * Wrapper chuẩn cho mọi HTTP response của BidVibe API.
 *
 * <pre>
 * {
 *   "success": true,
 *   "message": "OK",
 *   "data": { ... }
 * }
 * </pre>
 *
 * @param <T> kiểu dữ liệu của trường {@code data}.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // ------------------------------------------------------------------
    // Factory helpers
    // ------------------------------------------------------------------

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "Created", data);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
