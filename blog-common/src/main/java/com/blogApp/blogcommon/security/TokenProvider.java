package com.blogApp.blogcommon.security;

import com.blogApp.blogcommon.dto.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final JwtProperties jwtProperties;

    public String generateToken(Authentication authentication) {
        log.debug("Đang tạo token từ Authentication");
        Object principal = authentication.getPrincipal();
        
        log.debug("Principal class: {}", principal.getClass().getName());
        log.debug("Principal toString: {}", principal.toString());
        
        UserPrincipal userPrincipal;
        try {
            userPrincipal = (UserPrincipal) principal;
            log.debug("UserPrincipal đã được tạo thành công, id={}", userPrincipal.getId());
        } catch (ClassCastException e) {
            log.error("Lỗi khi ép kiểu từ {} sang UserPrincipal: {}", 
                     principal.getClass().getName(), e.getMessage());
            throw e;
        }
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        log.debug("Đang lấy userId từ token");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.parseLong(claims.getSubject());
        log.debug("UserId từ token: {}", userId);
        return userId;
    }

    public Long getJwtExpirationMs() {
        return jwtProperties.getAccessTokenExpirationMs();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createTokenFromUserId(Long userId) {
        log.debug("Đang tạo token từ userId: {}", userId);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}
