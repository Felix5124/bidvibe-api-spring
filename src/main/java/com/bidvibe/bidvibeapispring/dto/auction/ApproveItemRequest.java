package com.bidvibe.bidvibeapispring.dto.auction;

import com.bidvibe.bidvibeapispring.entity.Item;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Request body cho Admin duyệt đồ và xếp lịch phiên đấu giá.
 * POST /api/admin/items/approve
 */
@Getter
@Setter
@NoArgsConstructor
public class ApproveItemRequest {

    @NotNull(message = "ID vật phẩm không được để trống")
    private UUID itemId;

    /** Độ hiếm Admin gán sau khi thẩm định. */
    @NotNull(message = "Độ hiếm không được để trống")
    private Item.Rarity rarity;

    /** ID phiên đấu giá sẽ gán vật phẩm vào. */
    @NotNull(message = "ID phiên đấu giá không được để trống")
    private UUID sessionId;

    @NotNull(message = "Giá khởi điểm không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá khởi điểm phải lớn hơn 0")
    private BigDecimal startPrice;

    /** Bước giá cho English Auction (bỏ trống với Dutch/Sealed). */
    private BigDecimal stepPrice;

    /** Giá sàn cho Dutch Auction (bỏ trống với English/Sealed). */
    private BigDecimal minPrice;

    /** Số tiền giảm mỗi nấc cho Dutch Auction. */
    private BigDecimal decreaseAmount;

    /** Thứ tự đấu giá tuần tự trong phiên. */
    private Integer orderIndex;

    /** Thời điểm kết thúc (Dutch/Sealed); English tự tính từ startTime + duration. */
    @Future(message = "Thời gian kết thúc phải ở tương lai")
    private Instant endTime;
}
