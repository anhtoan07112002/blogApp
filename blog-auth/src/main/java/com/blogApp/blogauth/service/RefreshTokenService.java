package com.blogApp.blogauth.service;

import com.blogApp.blogauth.model.RefreshToken;
import com.blogApp.blogauth.repository.RefreshTokenRepository;
import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.exception.BlogException;
import com.blogApp.blogcommon.model.User;
import com.blogApp.blogcommon.service.CacheService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${app.auth.jwt.refreshTokenExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    @PersistenceContext
    private EntityManager entityManager;
    private CacheService cacheService;


    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        log.debug("Tạo refresh token cho user id: {}", user.getId());

        try {
            // Kiểm tra xem có token cũ trong database không
            String findTokenQuery = "SELECT token FROM refresh_tokens WHERE user_id = :userId";
            @SuppressWarnings("unchecked")
            java.util.List<String> oldTokens = entityManager.createNativeQuery(findTokenQuery)
                    .setParameter("userId", user.getId())
                    .getResultList();

            // Xóa các token cũ khỏi Redis cache
            for (String oldToken : oldTokens) {
                try {
                    boolean deleted = cacheService.delete(AppConstants.REFRESH_TOKEN_CACHE, oldToken);
                    log.debug("Xóa token cũ khỏi cache: {}, kết quả: {}", oldToken, deleted);
                } catch (Exception e) {
                    log.warn("Lỗi khi xóa token cũ khỏi cache: {}, lỗi: {}", oldToken, e.getMessage());
                }
            }

            // 2. Xóa token cũ từ database bằng native query
            int deleted = entityManager.createNativeQuery("DELETE FROM refresh_tokens WHERE user_id = :userId")
                    .setParameter("userId", user.getId())
                    .executeUpdate();

            if (deleted > 0) {
                log.debug("Đã xóa {} refresh token cũ từ database cho user id: {}", deleted, user.getId());
            }

            // Đảm bảo thay đổi được lưu vào database
            entityManager.flush();

            // 3. Tạo token mới
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(user);
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            refreshToken.setToken(UUID.randomUUID().toString());

            refreshToken = refreshTokenRepository.save(refreshToken);
            log.debug("Đã tạo refresh token mới: {}", refreshToken.getToken());
            return refreshToken;

        } catch (Exception e) {
            log.error("Lỗi khi tạo refresh token: {}", e.getMessage(), e);
            throw new BlogException("Không thể tạo refresh token: " + e.getMessage());
        }
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new BlogException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(User user) {
        return refreshTokenRepository.deleteByUser(user);
    }
}