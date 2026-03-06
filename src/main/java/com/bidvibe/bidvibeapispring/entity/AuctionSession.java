package com.bidvibe.bidvibeapispring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auction_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionSession {

    public enum Type {
        ENGLISH, DUTCH, SEALED
    }

    public enum Status {
        SCHEDULED, ACTIVE, PAUSED, COMPLETED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    /** Thời điểm bắt đầu phiên — có thể null nếu chưa lên lịch cụ thể. */
    @Column(name = "start_time")
    private Instant startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.SCHEDULED;

    /** Số giây còn lại của vật phẩm đang đấu giá — dùng khi resume paused session. */
    @Column(name = "remaining_seconds")
    private Integer remainingSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

