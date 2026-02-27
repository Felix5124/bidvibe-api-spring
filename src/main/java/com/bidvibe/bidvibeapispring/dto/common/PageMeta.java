package com.bidvibe.bidvibeapispring.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Metadata phân trang dùng chung, trả về cùng danh sách kết quả.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageMeta {

    /** Số trang hiện tại (0-based). */
    private int page;

    /** Kích thước trang. */
    private int size;

    /** Tổng số phần tử. */
    private long totalElements;

    /** Tổng số trang. */
    private int totalPages;
}
