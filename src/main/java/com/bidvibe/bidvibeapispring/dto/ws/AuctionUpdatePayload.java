package com.bidvibe.bidvibeapispring.dto.ws;

import com.bidvibe.bidvibeapispring.constant.WsEvents;
import com.bidvibe.bidvibeapispring.dto.user.UserSummary;
import com.bidvibe.bidvibeapispring.entity.Auction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket payload cho event: {@value WsEvents#AUCTION_UPDATE}.
 *
 * <p>Destination: {@code /topic/auction/{auctionId}}
 *
 * <p>Gửi đi mỗi khi có bid mới (English/Proxy), giá giảm nấc mới (Dutch),
 * hoặc trạng thái phiên thay đổi (ACTIVE → ENDED …).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionUpdatePayload {

    /** Tên event – dùng để client phân biệt loại message. */
    private final String event = WsEvents.AUCTION_UPDATE;

    private UUID auctionId;
    private BigDecimal currentPrice;
    private Auction.Status status;

    /** Người đang dẫn đầu (null nếu chưa có bid). */
    private UserSummary currentLeader;

    /** Thời điểm kết thúc cập nhật (Popcorn Bidding có thể thay đổi). */
    private Instant endTime;

    /** Tổng số lượt bid trong phiên. */
    private long totalBids;
}
