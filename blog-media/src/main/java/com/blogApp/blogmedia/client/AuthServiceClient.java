package com.blogApp.blogmedia.client;

import com.blogApp.blogcommon.dto.response.UserProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client để giao tiếp với Auth Service
 * - Lấy thông tin user
 * - Kiểm tra quyền truy cập
 * - Xác thực user
 */
import com.blogApp.blogcommon.dto.response.UserSummary;
import com.blogApp.blogcommon.dto.response.ApiResponse;
/**
 * Client interface để giao tiếp với Auth Service
 * - Chỉ lấy các thông tin cần thiết cho Post Service
 * - Có tích hợp circuit breaker để xử lý lỗi
 * - Có cơ chế retry khi gọi service thất bại
 */
@FeignClient(name = "auth-service", url = "${app.auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {

    /**
     * Lấy thông tin profile của user để hiển thị trong bài viết
     * @param username username của user cần lấy thông tin
     * @return UserProfile chứa thông tin cơ bản của user
     */
    @GetMapping("/api/auth/users/{username}")
    ApiResponse<UserProfile> getUserProfile(@PathVariable("username") String username);

    /**
     * Lấy thông tin user hiện tại từ token
     * - Dùng để xác thực và lấy role của user
     * @return UserSummary chứa thông tin user và role
     */
    @GetMapping("/api/auth/me")
    ApiResponse<UserSummary> getCurrentUser();
}