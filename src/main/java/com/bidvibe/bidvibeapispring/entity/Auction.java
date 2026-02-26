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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
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
    @Column(name = "interval_seconds")
    @Builder.Default
    private Integer intervalSeconds = 5;

    /** Thời điểm kết thúc (English/Dutch) hoặc thời điểm mở thầu (Sealed). */
    @Column(name = "end_time")
    private Instant endTime;

    /** Thứ tự đấu giá tuần tự trong phiên. */
    @Column(name = "order_index")
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;
}

