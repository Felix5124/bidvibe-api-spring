package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.rating.RatingResponse;
import com.bidvibe.bidvibeapispring.dto.user.UpdateProfileRequest;
import com.bidvibe.bidvibeapispring.dto.user.UserProfileResponse;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET  /api/users/me             – profile của chính mình
 * PUT  /api/users/me             – cập nhật profile
 * GET  /api/users/{id}           – profile công khai của user
 * GET  /api/users/{id}/ratings   – danh sách đánh giá
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(currentUser.getId())));
    }

    // PUT /api/users/me
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(currentUser.getId(), req)));
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(id)));
    }

    // GET /api/users/{id}/ratings?page=0&size=10
    @GetMapping("/{id}/ratings")
    public ResponseEntity<ApiResponse<PageResponse<RatingResponse>>> getUserRatings(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = userService.getUserRatings(id,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }
}
