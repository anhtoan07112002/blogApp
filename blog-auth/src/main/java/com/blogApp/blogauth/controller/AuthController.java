package com.blogApp.blogauth.controller;

import com.blogApp.blogauth.dto.request.RefreshTokenRequest;
import com.blogApp.blogauth.dto.response.TokenResponse;
import com.blogApp.blogauth.service.AuthService;
import com.blogApp.blogcommon.constant.SecurityConstants;
import com.blogApp.blogcommon.dto.response.UserPrincipal;
import com.blogApp.blogcommon.dto.request.LoginRequest;
import com.blogApp.blogcommon.dto.request.SignupRequest;
import com.blogApp.blogcommon.dto.response.ApiResponse;
import com.blogApp.blogcommon.dto.response.UserSummary;
import com.blogApp.blogcommon.exception.BadRequestException;
import com.blogApp.blogcommon.exception.BlogException;
import com.blogApp.blogcommon.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
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
    public ResponseEntity<ApiResponse<TokenResponse>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            log.debug("Bắt đầu xử lý đăng ký: {}", signUpRequest.getUsername());

            // Đăng ký người dùng
            User user = authService.registerUser(signUpRequest);
            log.debug("Đăng ký thành công, user_id={}", user.getId());

            // Tự động đăng nhập sau khi đăng ký
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsernameOrEmail(signUpRequest.getUsername());
            loginRequest.setPassword(signUpRequest.getPassword());

            log.debug("Bắt đầu xác thực người dùng sau đăng ký");
            TokenResponse tokenResponse = authService.authenticateUser(loginRequest);
            log.debug("Xác thực thành công, trả về tokenResponse: accessToken={}, refreshToken={}",
                    tokenResponse.getAccessToken().substring(0, 10) + "...",
                    tokenResponse.getRefreshToken().substring(0, 10) + "...");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created("Đăng ký người dùng thành công", tokenResponse));
        } catch (Exception ex) {
            log.error("Lỗi trong quá trình đăng ký: ", ex);
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

    @PostMapping("/signout")
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
