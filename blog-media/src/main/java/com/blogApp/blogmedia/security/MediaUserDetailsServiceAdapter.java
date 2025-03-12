package com.blogApp.blogmedia.security;

import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogcommon.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Lớp adapter kết nối MediaUserDetailsService với CustomUserDetailsService
 * - Đảm bảo MediaUserDetailsService hoạt động với TokenAuthenticationFilter
 * - Cung cấp các phương thức cần thiết cho xác thực
 * - Sử dụng thiết kế Adapter Pattern
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaUserDetailsServiceAdapter implements CustomUserDetailsService {

    private final MediaUserDetailsService mediaUserDetailsService;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("MediaUserDetailsServiceAdapter - loadUserByUsername({})", usernameOrEmail);
        return mediaUserDetailsService.loadUserByUsername(usernameOrEmail);
    }

    @Override
    public UserDetails loadUserById(Long id) throws ResourceNotFoundException {
        log.debug("MediaUserDetailsServiceAdapter - loadUserById({})", id);
        return mediaUserDetailsService.loadUserById(id);
    }
} 