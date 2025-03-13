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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Authentication", description = "API xác thực người dùng")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test")
    @PreAuthorize("permitAll()")
    @Operation(summary = "API test", description = "API kiểm tra kết nối")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Kết nối thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = String.class)))
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("Test", "Test"));
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Đăng nhập", 
            description = "Đăng nhập bằng username/email và mật khẩu")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Đăng nhập thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TokenResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Thông tin đăng nhập không hợp lệ")
    })
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
    @Operation(
            summary = "Đăng ký người dùng", 
            description = "Đăng ký tài khoản mới và trả về token để đăng nhập ngay")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Đăng ký thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TokenResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Dữ liệu không hợp lệ"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Lỗi server")
    })
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
    @Operation(
            summary = "Làm mới token", 
            description = "Tạo mới access token từ refresh token")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Làm mới token thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TokenResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Refresh token không hợp lệ hoặc hết hạn")
    })
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
    @Operation(
            summary = "Đăng xuất", 
            description = "Đăng xuất người dùng và vô hiệu hóa token",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Đăng xuất thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Token không hợp lệ"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Chưa xác thực")
    })
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
    @Operation(
            summary = "Lấy thông tin người dùng hiện tại", 
            description = "Lấy thông tin người dùng đã đăng nhập",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Lấy thông tin thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserSummary.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Chưa xác thực")
    })
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
