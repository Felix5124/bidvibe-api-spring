package com.bidvibe.bidvibeapispring.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper phân trang trả về danh sách kết quả kèm metadata.
 *
 * @param <T> kiểu phần tử trong danh sách.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private PageMeta meta;
}
