package com.blogApp.blogpost.security;

import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface đơn giản hóa cho việc xác thực user trong Post Service
 * - Chỉ cung cấp phương thức loadUserByUsername
 * - Không cần loadUserById vì Post Service không cần
 * - Sử dụng AuthServiceClient để lấy thông tin user từ Auth Service
 */
public interface PostUserDetailsService {
    @Transactional
    UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException;

    @Transactional
    UserDetails loadUserById(Long id) throws ResourceNotFoundException;
}