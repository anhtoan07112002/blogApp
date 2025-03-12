package com.blogApp.blogmedia.security;

import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Interface định nghĩa các phương thức cần thiết cho xác thực người dùng
 * trong Media Service
 * - loadUserByUsername: Tìm user theo username/email
 * - loadUserById: Tìm user theo ID
 */
public interface MediaUserDetailsService {
    
    /**
     * Tìm thông tin user từ Auth Service theo username/email
     * 
     * @param usernameOrEmail username hoặc email của user
     * @return UserDetails chứa thông tin của user
     * @throws UsernameNotFoundException khi không tìm thấy user
     */
    UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException;
    
    /**
     * Tìm thông tin user từ Auth Service theo id
     * 
     * @param id ID của user
     * @return UserDetails chứa thông tin của user
     * @throws ResourceNotFoundException khi không tìm thấy user
     */
    UserDetails loadUserById(Long id) throws ResourceNotFoundException;
} 