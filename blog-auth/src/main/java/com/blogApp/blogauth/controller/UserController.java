package com.blogApp.blogauth.controller;

import com.blogApp.blogauth.dto.request.ChangePasswordRequest;
import com.blogApp.blogauth.dto.request.ResetPasswordRequest;
import com.blogApp.blogauth.service.UserService;
import com.blogApp.blogcommon.dto.response.ApiResponse;
import com.blogApp.blogcommon.dto.response.UserProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Lấy thông tin profile của người dùng
     * @param username Tên đăng nhập
     * @return UserProfile chứa thông tin người dùng
     */
    @GetMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfile>> getUserProfile(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.success("Thông tin người dùng", userService.getUserProfile(username)));
    }

    /**
     * Đổi mật khẩu
     * @param userId ID của người dùng
     * @param request DTO chứa thông tin mật khẩu cũ và mới
     * @return ResponseEntity không có nội dung
     */
    @PostMapping("/{userId}/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    /**
     * Yêu cầu reset mật khẩu
     * @param email Email của người dùng
     * @return ResponseEntity không có nội dung
     */
    @PostMapping("/forgot-password")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Void>> initiatePasswordReset(@RequestParam String email) {
        userService.initiatePasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu reset mật khẩu đã được gửi", null));
    }

    /**
     * Reset mật khẩu
     * @param request DTO chứa token và mật khẩu mới
     * @return ResponseEntity không có nội dung
     */
    @PostMapping("/reset-password/confirm")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Reset mật khẩu thành công", null));
    }
} 