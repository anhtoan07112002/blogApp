package com.blogApp.blogauth.controller;

import com.blogApp.blogauth.dto.request.ChangePasswordRequest;
import com.blogApp.blogauth.dto.request.ResetPasswordRequest;
import com.blogApp.blogauth.service.UserService;
import com.blogApp.blogcommon.dto.response.ApiResponse;
import com.blogApp.blogcommon.dto.response.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "API quản lý thông tin người dùng")
public class UserController {

    private final UserService userService;

    /**
     * Lấy thông tin profile của người dùng
     * @param username Tên đăng nhập
     * @return UserProfile chứa thông tin người dùng
     */
    @GetMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Lấy thông tin người dùng", 
            description = "Lấy thông tin chi tiết của người dùng theo username",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Lấy thông tin thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserProfile.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy người dùng"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Chưa xác thực")
    })
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
    @Operation(
            summary = "Đổi mật khẩu", 
            description = "Đổi mật khẩu người dùng hiện tại",
            security = { @SecurityRequirement(name = "bearerAuth") },
            tags = { "Password" })
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Đổi mật khẩu thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Mật khẩu cũ không chính xác hoặc mật khẩu mới không hợp lệ"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Chưa xác thực"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện")
    })
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
    @Operation(
            summary = "Yêu cầu reset mật khẩu", 
            description = "Gửi email với link reset mật khẩu",
            tags = { "Password" })
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Yêu cầu reset mật khẩu đã được gửi"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy email")
    })
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
    @Operation(
            summary = "Xác nhận reset mật khẩu", 
            description = "Đặt lại mật khẩu mới bằng token",
            tags = { "Password" })
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Reset mật khẩu thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Token không hợp lệ hoặc hết hạn"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy token reset mật khẩu")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Reset mật khẩu thành công", null));
    }
} 