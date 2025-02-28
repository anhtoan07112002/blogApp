package com.blogApp.blogauth.controller;

import com.blogApp.blogauth.dto.request.RefreshTokenRequest;
import com.blogApp.blogauth.dto.response.TokenResponse;
import com.blogApp.blogauth.service.AuthService;
import com.blogApp.blogcommon.constant.SecurityConstants;
import com.blogApp.blogcommon.dto.UserPrincipal;
import com.blogApp.blogcommon.dto.request.LoginRequest;
import com.blogApp.blogcommon.dto.request.SignupRequest;
import com.blogApp.blogcommon.dto.response.ApiResponse;
import com.blogApp.blogcommon.dto.response.UserSummary;
import com.blogApp.blogcommon.exception.BadRequestException;
import com.blogApp.blogcommon.exception.BlogException;
import com.blogApp.blogcommon.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("Test", "Test"));
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<TokenResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            TokenResponse tokenResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", tokenResponse));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.unauthorized("Đăng nhập thất bại: " + ex.getMessage()));
        }
    }

    @PostMapping("/signup")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            User user = authService.registerUser(signUpRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created("Đăng ký người dùng thành công", user));
        } catch (BadRequestException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi đăng ký: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/refresh-token")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            TokenResponse tokenResponse = authService.refreshAccessToken(request);
            return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", tokenResponse));
        } catch (BadRequestException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(ex.getMessage()));
        } catch (BlogException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi làm mới token: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutUser(
            @RequestHeader(value = SecurityConstants.HEADER_STRING, required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest("Invalid authorization header"));
        }
        
        try {
            String token = authHeader.substring(7);
            authService.logoutUser(token);
            return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
        } catch (BlogException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi đăng xuất: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserSummary>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            UserSummary userSummary = UserSummary.builder()
                    .id(currentUser.getId())
                    .username(currentUser.getUsername())
                    .email(currentUser.getEmail())
                    .role(currentUser.getAuthorities().iterator().next().getAuthority())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Thông tin người dùng hiện tại", userSummary));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin người dùng: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
