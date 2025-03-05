package com.blogApp.blogpost.client;

import com.blogApp.blogcommon.dto.response.UserProfile;
import com.blogApp.blogpost.config.FeignClientConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * Client interface để giao tiếp với Auth Service
 * - Sử dụng FeignClient để tạo HTTP client
 * - Có tích hợp circuit breaker để xử lý lỗi
 * - Có cơ chế retry khi gọi service thất bại
 */
@FeignClient(
        name = "blog-auth-service",
        path = "/api/auth",
        configuration = FeignClientConfig.class
)
public interface AuthServiceClient {

    /**
     * Lấy thông tin user profile từ Auth Service
     * - Có circuit breaker để fallback khi service lỗi
     * - Có retry khi gọi thất bại
     * @param userId ID của user cần lấy thông tin
     * @return Optional<UserProfile> chứa thông tin user nếu tìm thấy
     */
    @GetMapping("/users/{userId}")
    @CircuitBreaker(name = "auth-service", fallbackMethod = "getUserInfoFallback")
    @Retry(name = "auth-service")
    Optional<UserProfile> getUserInfo(@PathVariable("userId") String userId);

    /**
     * Phương thức fallback khi không thể gọi Auth Service
     * @return Optional.empty() để xử lý gracefully
     */
    default Optional<UserProfile> getUserInfoFallback(String userId, Exception exception) {
        return Optional.empty();
    }
}