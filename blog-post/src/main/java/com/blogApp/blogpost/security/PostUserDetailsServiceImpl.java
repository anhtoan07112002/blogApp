package com.blogApp.blogpost.security;

import com.blogApp.blogcommon.dto.UserPrincipal;
import com.blogApp.blogcommon.dto.response.UserSummary;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogpost.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Implementation của PostUserDetailsService
 * - Sử dụng AuthServiceClient để lấy thông tin user từ Auth Service
 * - Không cần cache vì Auth Service đã có cache
 * - Chỉ cần thông tin cơ bản của user để xác thực và phân quyền
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostUserDetailsServiceImpl implements PostUserDetailsService {

    private final AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        try {
            log.debug("Đang lấy thông tin user từ Auth Service với username/email: {}", usernameOrEmail);
            // Không có endpoint trực tiếp nên dùng getUserProfile
            // Trong thực tế nên tạo endpoint riêng cho việc này
            UserSummary userSummary = authServiceClient.getCurrentUser().getData();
            return createUserPrincipalFromUserSummary(userSummary);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user từ Auth Service: {}", e.getMessage(), e);
            throw new UsernameNotFoundException("Error loading user from auth service", e);
        }
    }

    @Override
    @Transactional
    public UserDetails loadUserById(Long id) throws ResourceNotFoundException {
        try {
            log.debug("Đang lấy thông tin user từ Auth Service với id: {}", id);
            // Sử dụng endpoint /me vì chúng ta không có endpoint /users/{id}
            UserSummary userSummary = authServiceClient.getCurrentUser().getData();
            return createUserPrincipalFromUserSummary(userSummary);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user từ Auth Service: {}", e.getMessage(), e);
            throw new ResourceNotFoundException("User", "id", id);
        }
    }
    
    /**
     * Tạo UserPrincipal từ UserSummary
     * @param userSummary thông tin cơ bản của user
     * @return UserPrincipal
     */
    private UserPrincipal createUserPrincipalFromUserSummary(UserSummary userSummary) {
        return new UserPrincipal(
                userSummary.getId(),
                userSummary.getUsername(),
                userSummary.getEmail(),
                "", // Không cần password vì chúng ta chỉ xác thực bằng token
                Collections.singletonList(new SimpleGrantedAuthority(userSummary.getRole()))
        );
    }
}