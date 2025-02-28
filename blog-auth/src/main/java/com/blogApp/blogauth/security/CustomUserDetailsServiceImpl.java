package com.blogApp.blogauth.security;

import com.blogApp.blogauth.repository.UserRepository;
import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.dto.UserPrincipal;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogcommon.model.User;
import com.blogApp.blogcommon.security.CustomUserDetailsService;
import com.blogApp.blogcommon.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        Object userInCache = cacheService.get(AppConstants.USERS_CACHE, usernameOrEmail);
        if (userInCache != null) {
            return UserPrincipal.create((User) userInCache);
        }

        User user1 = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("Không tìm thấy người dùng với username hoặc email: " + usernameOrEmail));
        
        return UserPrincipal.create(user1);
    }

    @Override
    @Transactional
    public UserDetails loadUserById(Long id) {
        Object userInCache = cacheService.get(AppConstants.USERS_CACHE, String.valueOf(id));
        if (userInCache != null) {
            return UserPrincipal.create((User) userInCache);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> 
                    new ResourceNotFoundException("User", "id", id));
        
        return UserPrincipal.create(user);
    }
} 