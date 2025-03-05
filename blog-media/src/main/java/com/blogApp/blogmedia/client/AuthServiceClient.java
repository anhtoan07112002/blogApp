package com.blogApp.blogmedia.client;

import com.blogApp.blogcommon.dto.response.UserProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * Client để giao tiếp với Auth Service
 * - Lấy thông tin user
 * - Kiểm tra quyền truy cập
 * - Xác thực user
 */
@FeignClient(name = "auth-service", url = "${app.auth-service.url}")
public interface AuthServiceClient {
    
    /**
     * Lấy thông tin user theo ID
     * @param userId ID của user
     * @return UserProfile chứa thông tin user
     */
    @GetMapping("/users/{userId}")
    Optional<UserProfile> getUserInfo(@PathVariable("userId") String userId);
    
    /**
     * Kiểm tra quyền truy cập media
     * @param userId ID của user
     * @param mediaId ID của media
     * @return true nếu có quyền, false nếu không
     */
    @GetMapping("/users/{userId}/media/{mediaId}/access")
    boolean checkMediaAccess(@PathVariable("userId") String userId, @PathVariable("mediaId") String mediaId);
} 