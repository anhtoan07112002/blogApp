package com.blogApp.blogpost.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Cấu hình cho Feign Client
 * - Thêm interceptor để chuyển tiếp headers giữa các service
 * - Đảm bảo các thông tin xác thực được giữ nguyên
 */
@Configuration
public class FeignClientConfig {

    /**
     * Tạo interceptor để chuyển tiếp Authorization header
     * - Lấy header từ request hiện tại
     * - Thêm vào request của Feign Client
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtToken) {
                requestTemplate.header("Authorization", "Bearer " + jwtToken.getToken().getTokenValue());
            }
        };
    }
}
