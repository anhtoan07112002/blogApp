package com.blogApp.blogpost.security;

import com.blogApp.blogcommon.dto.UserPrincipal;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogcommon.security.CustomUserDetailsService;
import com.blogApp.blogcommon.dto.response.UserProfile;
import com.blogApp.blogcommon.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter class để chuyển đổi PostUserDetailsService thành CustomUserDetailsService
 * - Giữ nguyên logic của PostUserDetailsService
 * - Implement thêm phương thức loadUserById để thỏa mãn interface
 * - Không ảnh hưởng đến blog-auth service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostUserDetailsServiceAdapter implements CustomUserDetailsService {

    private final PostUserDetailsService postUserDetailsService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return postUserDetailsService.loadUserByUsername(usernameOrEmail);
    }

    @Override
    @Transactional
    public UserDetails loadUserById(Long id) {
        try {
            log.debug("Đang lấy thông tin user từ PostUserDetailsService với id: {}", id);
            return postUserDetailsService.loadUserById(id);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user từ PostUserDetailsService: {}", e.getMessage());
            throw new ResourceNotFoundException("User", "id", id);
        }
    }
} 