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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new BlogException("User not found with username: " + loginRequest.getUsernameOrEmail()));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        cacheService.set(
                AppConstants.REFRESH_TOKEN_CACHE,
                refreshToken.getToken(),
                refreshToken,
                refreshTokenExpirationMs / 1000,
                TimeUnit.SECONDS
        );

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

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getJwtExpirationMs())
                .build();
    }

    @Transactional
    public User registerUser(SignupRequest signUpRequest) {

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

        // Creating user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);

        user.setRole(RoleName.ROLE_USER);
        return userRepository.save(user);
    }

    public TokenResponse refreshAccessToken(RefreshTokenRequest request) {
        Object cachedRefreshToken = cacheService.get(AppConstants.REFRESH_TOKEN_CACHE, request.getRefreshToken());
        RefreshToken refreshToken;

        if (cachedRefreshToken != null) {
            refreshToken = (RefreshToken) cachedRefreshToken;
            if (refreshToken.isExpired()) {
                cacheService.delete(AppConstants.REFRESH_TOKEN_CACHE, refreshToken.getToken());
                throw new BadRequestException("Refresh token was expired. Please sign in again");
            }
        } else {
            return refreshTokenService.findByToken(request.getRefreshToken())
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
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
        return null;
    }

    public void logoutUser(String token) {
        Long userId = tokenProvider.getUserIdFromToken(token);
        if (userId == null) {
            throw new BlogException("Token không hợp lệ hoặc đã hết hạn");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlogException("Không tìm thấy người dùng với id: " + userId));

        // Xóa refresh token của người dùng
        refreshTokenService.deleteByUserId(user);
        
        // Thêm access token vào blacklist
        Long expiration = tokenProvider.getJwtExpirationMs();
        Long currentTime = Instant.now().getEpochSecond();
        Long ttl = expiration - currentTime;

        if (ttl > 0) {
            cacheService.set(
                    AppConstants.ACCESS_TOKEN_BLACKLIST_CACHE,
                    token,
                    "BLACKLISTED",
                    ttl,
                    TimeUnit.SECONDS
            );
        }
    }
}
