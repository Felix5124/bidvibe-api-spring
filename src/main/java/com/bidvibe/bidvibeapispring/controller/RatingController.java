package com.bidvibe.bidvibeapispring.controller;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.rating.CreateRatingRequest;
import com.bidvibe.bidvibeapispring.dto.rating.RatingResponse;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Rating & Feedback APIs
 * POST /api/ratings – tạo đánh giá sau giao dịch
 * GET /api/ratings/user/{userId} – lấy đánh giá của user
 */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // POST /api/ratings
    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> createRating(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateRatingRequest req) {
        var result = ratingService.createRating(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    // GET /api/ratings/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getUserRatings(@PathVariable UUID userId) {
        var result = ratingService.getRatingsByUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
