package com.bidvibe.bidvibeapispring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.rating.RatingResponse;
import com.bidvibe.bidvibeapispring.dto.user.UpdateProfileRequest;
import com.bidvibe.bidvibeapispring.dto.user.UserProfileResponse;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
import com.bidvibe.bidvibeapispring.repository.RatingRepository;
import com.bidvibe.bidvibeapispring.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Xử lý nghiệp vụ liên quan đến User:
 * - Lấy / cập nhật profile
 * - Tự động cập nhật điểm uy tín sau khi có rating mới
 * - Tạo User mới khi lần đầu đăng nhập qua Supabase JWT
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final WsEventPublisher wsEventPublisher;

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Trả về profile của user đang đăng nhập.
     * GET /api/users/profile
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = findById(userId);
        return UserProfileResponse.from(user);
    }

    /**
     * Cập nhật nickname, avatarUrl, phone, address.
     * PUT /api/users/profile
     */
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = findById(userId);

        if (req.getNickname()   != null) user.setNickname(req.getNickname());
        if (req.getAvatarUrl()  != null) user.setAvatarUrl(req.getAvatarUrl());
        if (req.getPhone()      != null) user.setPhone(req.getPhone());
        if (req.getAddress()    != null) user.setAddress(req.getAddress());

        return UserProfileResponse.from(userRepository.save(user));
    }

    /**
     * Tìm hoặc tạo User từ claims Supabase JWT.
     * {@code sub} là UUID duy nhất của user trong Supabase Auth.
     * {@code email} dùng để tra cứu User trong database nội bộ.
     * Gọi từ JwtAuthFilter mỗi request cần xác thực.
     */
    @Transactional
    public User findOrCreate(String sub, String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Derive a safe default nickname from the email prefix
                    String defaultNickname = email.contains("@")
                            ? email.substring(0, email.indexOf('@'))
                            : email;
                    return userRepository.save(
                            User.builder()
                                    .email(email)
                                    .nickname(defaultNickname)
                                    .role(User.Role.USER)
                                    .build()
                    );
                });
    }

    /**
     * Tính lại điểm uy tín trung bình và lưu vào profile.
     * Gọi sau mỗi lần Rating được tạo mới.
     */
    @Transactional
    public void refreshReputationScore(UUID userId) {
        User user = findById(userId);
        Double avg = userRepository.calculateAverageRating(userId);
        if (avg != null) {
            user.setReputationScore(
                    BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            userRepository.save(user);
        }
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BidVibeException(ErrorCode.USER_NOT_FOUND));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BidVibeException(ErrorCode.USER_NOT_FOUND));
    }

    // ------------------------------------------------------------------
    // Public – user ratings
    // ------------------------------------------------------------------

    /** GET /api/users/{id}/ratings – danh sách đánh giá nhận được (paginated). */
    @Transactional(readOnly = true)
    public Page<RatingResponse> getUserRatings(UUID userId, Pageable pageable) {
        var list = ratingRepository.findByToUserIdOrderByCreatedAtDesc(userId)
                .stream().map(RatingResponse::from).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return new org.springframework.data.domain.PageImpl<>(
                start <= list.size() ? list.subList(start, end) : java.util.List.of(),
                pageable, list.size());
    }

    // ------------------------------------------------------------------
    // Admin – user management
    // ------------------------------------------------------------------

    /** GET /api/admin/users – tìm kiếm và lọc danh sách users. */
    @Transactional(readOnly = true)
    public Page<UserProfileResponse> adminListUsers(String search, User.Role role,
                                                     Boolean isBanned, Boolean isMuted,
                                                     Pageable pageable) {
        return userRepository.searchUsers(search, role, isBanned, isMuted, pageable)
                .map(UserProfileResponse::from);
    }

    /** PATCH /api/admin/users/{id}/role – cấp / thu hồi quyền ADMIN. */
    @Transactional
    public UserProfileResponse changeRole(UUID userId, User.Role newRole) {
        User user = findById(userId);
        user.setRole(newRole);
        return UserProfileResponse.from(userRepository.save(user));
    }

    /** POST /api/admin/users/{id}/mute – tắt chat của user. */
    @Transactional
    public void muteUser(UUID userId) {
        User user = findById(userId);
        user.setMuted(true);
        userRepository.save(user);
    }

    /** POST /api/admin/users/{id}/unmute – bật chat của user. */
    @Transactional
    public void unmuteUser(UUID userId) {
        User user = findById(userId);
        user.setMuted(false);
        userRepository.save(user);
    }

    /** POST /api/admin/users/{id}/ban – ban user. */
    @Transactional
    public void banUser(UUID userId) {
        User user = findById(userId);
        user.setBanned(true);
        user.setBannedAt(Instant.now());
        userRepository.save(user);
    }

    /** POST /api/admin/users/{id}/unban – unban user. */
    @Transactional
    public void unbanUser(UUID userId) {
        User user = findById(userId);
        user.setBanned(false);
        user.setBannedAt(null);
        userRepository.save(user);
    }

    /**
     * POST /api/admin/users/{id}/kick – ngắt kết nối WebSocket của user khỏi một phòng đấu giá.
     * Gửi tín hiệu WS; client tự disconnect. User vẫn có thể reconnect.
     */
    public void kickUser(UUID userId, UUID auctionId) {
        wsEventPublisher.publishKick(userId, auctionId);
    }
}
