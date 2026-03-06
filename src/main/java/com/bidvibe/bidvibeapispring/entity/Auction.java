package com.bidvibe.bidvibeapispring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    public enum Status {
        WAITING, ACTIVE, ENDED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AuctionSession session;

    /**
     * Một item có thể xuất hiện trong nhiều auction qua các phiên khác nhau,
     * nhưng chỉ có 1 WAITING/ACTIVE tại một thời điểm (partial unique index trong DB).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** Giá khởi điểm (English/Dutch) hoặc giá tối thiểu (Sealed). */
    @Column(name = "start_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal startPrice;

    /** Giá cao nhất hiện tại (English) hoặc giá hiện tại (Dutch). */
    @Column(name = "current_price", precision = 19, scale = 4)
    private BigDecimal currentPrice;

    /** Giá sàn cho Dutch Auction. */
    @Column(name = "min_price", precision = 19, scale = 4)
    private BigDecimal minPrice;

    /** Bước giá cho English Auction. */
    @Column(name = "step_price", precision = 19, scale = 4)
    private BigDecimal stepPrice;

    /** Số tiền giảm mỗi nấc cho Dutch Auction. */
    @Column(name = "decrease_amount", precision = 19, scale = 4)
    private BigDecimal decreaseAmount;

    /** Thời gian giữa mỗi nấc giảm cho Dutch (giây), mặc định 5s. */
    @Column(name = "interval_seconds", nullable = false)
    @Builder.Default
    private Integer intervalSeconds = 5;

    /** Tổng thời gian đấu giá (giây), mặc định 120s. */
    @Column(name = "duration_seconds", nullable = false)
    @Builder.Default
    private Integer durationSeconds = 120;

    /** Số giây gia hạn khi có bid cuối (Popcorn Bidding), mặc định 30s. */
    @Column(name = "extend_seconds", nullable = false)
    @Builder.Default
    private Integer extendSeconds = 30;

    /** Thời điểm kết thúc (English/Dutch) hoặc thời điểm mở thầu (Sealed). */
    @Column(name = "end_time")
    private Instant endTime;

    /** Thứ tự đấu giá tuần tự trong phiên. */
    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    /** Optimistic Locking — chống race condition khi đặt giá đồng thời. */
    @Version
    @Column(nullable = false)
    private Long version;
}

