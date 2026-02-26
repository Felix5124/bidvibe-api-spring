package com.bidvibe.bidvibeapispring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    public enum Rarity {
        COMMON, RARE, LEGENDARY
    }

    public enum Status {
        PENDING, APPROVED, IN_AUCTION, IN_INVENTORY, SHIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_owner_id")
    private User currentOwner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Mảng link ảnh lưu dạng JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "jsonb")
    private List<String> imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Rarity rarity = Rarity.COMMON;

    /**
     * Giá rao bán trên Chợ Đen – null nếu không rao bán.
     */
    @Column(name = "asking_price", precision = 19, scale = 4)
    private BigDecimal askingPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    /**
     * Khóa chuyển nhượng 12h sau mỗi giao dịch.
     */
    @Column(name = "cooldown_until")
    private Instant cooldownUntil;
}

