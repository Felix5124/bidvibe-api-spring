package com.bidvibe.bidvibeapispring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public enum Role {
        USER, ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "reputation_score", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal reputationScore = new BigDecimal("5.0");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

