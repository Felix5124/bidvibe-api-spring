package com.bidvibe.bidvibeapispring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidvibe.bidvibeapispring.constant.ErrorCode;
import com.bidvibe.bidvibeapispring.dto.user.UpdateProfileRequest;
import com.bidvibe.bidvibeapispring.dto.user.UserProfileResponse;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.exception.BidVibeException;
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
     * Tìm hoặc tạo User từ email Supabase JWT.
     * Gọi từ JwtAuthFilter mỗi request cần xác thực.
     */
    @Transactional
    public User findOrCreate(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .role(User.Role.USER)
                                .build()
                ));
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
}
