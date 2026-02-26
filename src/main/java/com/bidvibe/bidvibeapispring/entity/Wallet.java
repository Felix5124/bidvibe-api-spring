package com.bidvibe.bidvibeapispring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "balance_available", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balanceAvailable = BigDecimal.ZERO;

    @Column(name = "balance_locked", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balanceLocked = BigDecimal.ZERO;

    /**
     * Optimistic Locking – chống race condition khi cộng/trừ tiền.
     */
    @Version
    @Column(nullable = false)
    private Long version;
}

