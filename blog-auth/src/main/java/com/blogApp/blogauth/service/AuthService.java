package com.blogApp.blogauth.service;


import com.blogApp.blogauth.dto.request.RefreshTokenRequest;
import com.blogApp.blogauth.dto.response.TokenResponse;
import com.blogApp.blogauth.model.RefreshToken;
import com.blogApp.blogauth.repository.UserRepository;
import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.dto.request.LoginRequest;
import com.blogApp.blogcommon.dto.request.SignupRequest;
import com.blogApp.blogcommon.enums.AuthProvider;
import com.blogApp.blogcommon.enums.RoleName;
import com.blogApp.blogcommon.exception.BadRequestException;
import com.blogApp.blogcommon.exception.BlogException;
import com.blogApp.blogcommon.model.User;
import com.blogApp.blogcommon.security.TokenProvider;
import com.blogApp.blogcommon.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CacheService cacheService;

    @Value("${app.auth.jwt.refreshTokenExpirationMs}")
    private Long refreshTokenExpirationMs;

    @Value("${app.auth.jwt.accessTokenExpirationMs}")
    private Long accessTokenExpirationMs;

    public TokenResponse authenticateUser(LoginRequest loginRequest) {
        log.debug("Xác thực người dùng: {}", loginRequest.getUsernameOrEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            log.debug("Xác thực thành công cho: {}", loginRequest.getUsernameOrEmail());
            log.debug("AuthenticationClass: {}, PrincipalClass: {}",
                     authentication.getClass().getName(),
                     authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null");

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = tokenProvider.generateToken(authentication);
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail())
                    .orElseThrow(() -> new BlogException("User not found with username: " + loginRequest.getUsernameOrEmail()));

            log.debug("Tạo RefreshToken cho user id: {}", user.getId());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            log.debug("Lưu RefreshToken vào cache: {}", refreshToken.getToken());
            cacheService.set(
                    AppConstants.REFRESH_TOKEN_CACHE,
                    refreshToken.getToken(),
                    refreshToken,
                    refreshTokenExpirationMs / 1000,
                    TimeUnit.SECONDS
            );

            if (!cacheService.hasKey(AppConstants.USERS_CACHE, user.getUsername()) && !cacheService.hasKey(AppConstants.USERS_CACHE, user.getId().toString())) {
                log.debug("Lưu User vào cache: username={}, id={}", user.getUsername(), user.getId());
                cacheService.set(
                        AppConstants.USERS_CACHE,
                        user.getUsername(),
                        user,
                        1,
                        TimeUnit.DAYS
                );

                cacheService.set(
                        AppConstants.USERS_CACHE,
                        user.getId().toString(),
                        user,
                        1,
                        TimeUnit.DAYS
                );
            }

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getJwtExpirationMs())
                    .build();
        } catch (Exception e) {
            log.error("Lỗi xác thực: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public User registerUser(SignupRequest signUpRequest) {
        log.debug("Đăng ký người dùng mới: {}", signUpRequest.getUsername());

        if (signUpRequest.getUsername() == null || signUpRequest.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username không được để trống!");
        }

        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email không được để trống!");
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email Address already in use!");
        }

        log.debug("Tạo tài khoản người dùng: {}", signUpRequest.getUsername());
        // Creating user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);

        // Xử lý trường name nếu có
        if (signUpRequest.getName() != null && !signUpRequest.getName().trim().isEmpty()) {
            String fullName = signUpRequest.getName().trim();
            // Tách họ và tên nếu có thể
            int lastSpaceIndex = fullName.lastIndexOf(' ');
            if (lastSpaceIndex > 0) {
                user.setFirstName(fullName.substring(0, lastSpaceIndex).trim());
                user.setLastName(fullName.substring(lastSpaceIndex + 1).trim());
            } else {
                // Nếu chỉ có một từ, đặt vào lastName
                user.setLastName(fullName);
            }
        } else {
            // Sử dụng firstName và lastName nếu có
            if (signUpRequest.getFirstName() != null) {
                user.setFirstName(signUpRequest.getFirstName());
            }
            if (signUpRequest.getLastName() != null) {
                user.setLastName(signUpRequest.getLastName());
            }
        }

        user.setRole(RoleName.ROLE_USER);
        User savedUser = userRepository.save(user);
        log.debug("Đã đăng ký thành công người dùng: id={}, username={}", savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }

    public TokenResponse refreshAccessToken(RefreshTokenRequest request) {
        log.debug("Yêu cầu làm mới token: {}", request.getRefreshToken());
        Object cachedRefreshToken = cacheService.get(AppConstants.REFRESH_TOKEN_CACHE, request.getRefreshToken());

        if (cachedRefreshToken != null) {
            log.debug("Tìm thấy refreshToken trong cache, kiểu dữ liệu: {}", cachedRefreshToken.getClass().getName());
            try {
                RefreshToken refreshToken = (RefreshToken) cachedRefreshToken;
                if (refreshToken.isExpired()) {
                    log.debug("RefreshToken đã hết hạn: {}", refreshToken.getExpiryDate());
                    cacheService.delete(AppConstants.REFRESH_TOKEN_CACHE, refreshToken.getToken());
                    throw new BadRequestException("Refresh token was expired. Please sign in again");
                }

                User user = refreshToken.getUser();
                log.debug("Tạo accessToken mới cho user: id={}, username={}", user.getId(), user.getUsername());
                String accessToken = tokenProvider.createTokenFromUserId(user.getId());

                return TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(request.getRefreshToken())
                        .tokenType("Bearer")
                        .expiresIn(tokenProvider.getJwtExpirationMs())
                        .build();
            } catch (ClassCastException e) {
                log.error("Lỗi ép kiểu RefreshToken: {}", e.getMessage(), e);
                log.debug("Xóa refreshToken không hợp lệ khỏi cache: {}", request.getRefreshToken());
                cacheService.delete(AppConstants.REFRESH_TOKEN_CACHE, request.getRefreshToken());
                throw new BadRequestException("Invalid refresh token format in cache");
            }
        } else {
            log.debug("Không tìm thấy refreshToken trong cache, tìm trong database");
            return refreshTokenService.findByToken(request.getRefreshToken())
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        log.debug("Tạo accessToken mới từ database cho user: id={}", user.getId());
                        String accessToken = tokenProvider.createTokenFromUserId(user.getId());

                        return TokenResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(request.getRefreshToken())
                                .tokenType("Bearer")
                                .expiresIn(tokenProvider.getJwtExpirationMs())
                                .build();
                    })
                    .orElseThrow(() -> new BlogException("Refresh token not found in database!"));
        }
    }

    public void logoutUser(String token) {
        log.debug("Xử lý đăng xuất với token");
        Long userId = tokenProvider.getUserIdFromToken(token);
        if (userId == null) {
            throw new BlogException("Token không hợp lệ hoặc đã hết hạn");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlogException("Không tìm thấy người dùng với id: " + userId));

        log.debug("Đăng xuất user: id={}, username={}", user.getId(), user.getUsername());

        // Xóa refresh token của người dùng
        refreshTokenService.deleteByUserId(user);

        // Thêm access token vào blacklist
        Long expiration = tokenProvider.getJwtExpirationMs() / 1000;
        Long currentTime = Instant.now().getEpochSecond();
        Long ttl = expiration - currentTime;

        if (ttl > 0) {
            log.debug("Thêm access token vào blacklist với TTL: {} giây", ttl);
            cacheService.set(
                    AppConstants.ACCESS_TOKEN_BLACKLIST_CACHE,
                    token,
                    "BLACKLISTED",
                    ttl,
                    TimeUnit.SECONDS
            );
        }
        log.debug("Đăng xuất thành công");
    }
}
