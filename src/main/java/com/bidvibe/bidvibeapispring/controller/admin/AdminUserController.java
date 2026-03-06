package com.bidvibe.bidvibeapispring.controller.admin;

import com.bidvibe.bidvibeapispring.dto.common.ApiResponse;
import com.bidvibe.bidvibeapispring.dto.common.PageResponse;
import com.bidvibe.bidvibeapispring.dto.user.BanUserRequest;
import com.bidvibe.bidvibeapispring.dto.user.ChangeRoleRequest;
import com.bidvibe.bidvibeapispring.dto.user.KickUserRequest;
import com.bidvibe.bidvibeapispring.dto.user.UserProfileResponse;
import com.bidvibe.bidvibeapispring.entity.User;
import com.bidvibe.bidvibeapispring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET  /api/admin/users                 – danh sách user (phân trang, tìm kiếm)
 * GET  /api/admin/users/{id}            – chi tiết user
 * PUT  /api/admin/users/{id}/role       – đổi vai trò
 * POST /api/admin/users/{id}/mute       – tắt tiếng
 * POST /api/admin/users/{id}/unmute     – bỏ tắt tiếng
 * POST /api/admin/users/{id}/ban        – khóa tài khoản
 * POST /api/admin/users/{id}/unban      – mở khóa tài khoản
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // GET /api/admin/users?search=&role=USER&isBanned=false&isMuted=false&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) Boolean isBanned,
            @RequestParam(required = false) Boolean isMuted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = userService.adminListUsers(search, role, isBanned, isMuted,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(result)));
    }

    // GET /api/admin/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(id)));
    }

    // PATCH /api/admin/users/{id}/role
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserProfileResponse>> changeRole(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeRoleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.changeRole(id, req.getRole())));
    }

    // POST /api/admin/users/{id}/mute
    @PostMapping("/{id}/mute")
    public ResponseEntity<ApiResponse<Void>> muteUser(@PathVariable UUID id) {
        userService.muteUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // POST /api/admin/users/{id}/unmute
    @PostMapping("/{id}/unmute")
    public ResponseEntity<ApiResponse<Void>> unmuteUser(@PathVariable UUID id) {
        userService.unmuteUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // POST /api/admin/users/{id}/ban
    @PostMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) BanUserRequest req) {
        userService.banUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // POST /api/admin/users/{id}/unban
    @PostMapping("/{id}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable UUID id) {
        userService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // POST /api/admin/users/{id}/kick
    @PostMapping("/{id}/kick")
    public ResponseEntity<ApiResponse<Void>> kickUser(
            @PathVariable UUID id,
            @Valid @RequestBody KickUserRequest req) {
        userService.kickUser(id, req.getAuctionId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
