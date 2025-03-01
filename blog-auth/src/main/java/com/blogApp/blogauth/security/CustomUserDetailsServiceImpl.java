package com.blogApp.blogauth.security;

import com.blogApp.blogauth.repository.UserRepository;
import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.dto.response.UserPrincipal;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogcommon.model.User;
import com.blogApp.blogcommon.security.CustomUserDetailsService;
import com.blogApp.blogcommon.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("loadUserByUsername bắt đầu với: {}", usernameOrEmail);
        User user = null;
        
        // Thử lấy từ cache, xử lý an toàn hơn
        try {
            log.debug("Tìm user trong cache với key: {}", usernameOrEmail);
            Object userInCache = cacheService.get(AppConstants.USERS_CACHE, usernameOrEmail);
            if (userInCache != null) {
                log.debug("Đã tìm thấy đối tượng trong cache, kiểu: {}", userInCache.getClass().getName());
                if (userInCache instanceof User) {
                    user = (User) userInCache;
                    log.debug("Đối tượng User từ cache có id={}, username={}", user.getId(), user.getUsername());
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    log.debug("Đã tạo UserPrincipal từ User cache: id={}, class={}", 
                             userPrincipal.getId(), userPrincipal.getClass().getName());
                    return userPrincipal;
                } else {
                    log.warn("Object in cache is not of type User: {}", userInCache.getClass().getName());
                    // Xóa dữ liệu cache không hợp lệ
                    cacheService.delete(AppConstants.USERS_CACHE, usernameOrEmail);
                }
            } else {
                log.debug("Không tìm thấy user trong cache");
            }
        } catch (Exception e) {
            log.error("Error loading user from cache: {}", e.getMessage(), e);
            // Xóa dữ liệu cache có thể gây lỗi
            try {
                cacheService.delete(AppConstants.USERS_CACHE, usernameOrEmail);
            } catch (Exception ex) {
                log.warn("Error deleting invalid cache: {}", ex.getMessage());
            }
        }

        // Nếu không lấy được từ cache, lấy từ database
        log.debug("Tìm user trong database với username/email: {}", usernameOrEmail);
        user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy user với username/email: {}", usernameOrEmail);
                    return new UsernameNotFoundException("Không tìm thấy người dùng với username hoặc email: " + usernameOrEmail);
                });
        
        log.debug("Đã tìm thấy user trong database: id={}, username={}", user.getId(), user.getUsername());
        
        // Lưu vào cache
        try {
            log.debug("Lưu user vào cache với key username={}", usernameOrEmail);
            cacheService.set(AppConstants.USERS_CACHE, usernameOrEmail, user, 1, java.util.concurrent.TimeUnit.DAYS);
            log.debug("Lưu user vào cache với key id={}", user.getId().toString());
            cacheService.set(AppConstants.USERS_CACHE, user.getId().toString(), user, 1, java.util.concurrent.TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Error caching user: {}", e.getMessage(), e);
        }
        
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        log.debug("Đã tạo UserPrincipal từ database: id={}, class={}", 
                 userPrincipal.getId(), userPrincipal.getClass().getName());
        return userPrincipal;
    }

    @Override
    @Transactional
    public UserDetails loadUserById(Long id) {
        log.debug("loadUserById bắt đầu với id: {}", id);
        User user = null;
        String idStr = String.valueOf(id);
        
        // Thử lấy từ cache, xử lý an toàn hơn
        try {
            log.debug("Tìm user trong cache với id: {}", idStr);
            Object userInCache = cacheService.get(AppConstants.USERS_CACHE, idStr);
            if (userInCache != null) {
                log.debug("Đã tìm thấy đối tượng trong cache, kiểu: {}", userInCache.getClass().getName());
                if (userInCache instanceof User) {
                    user = (User) userInCache;
                    log.debug("Đối tượng User từ cache có id={}, username={}", user.getId(), user.getUsername());
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    log.debug("Đã tạo UserPrincipal từ User cache: id={}, class={}", 
                             userPrincipal.getId(), userPrincipal.getClass().getName());
                    return userPrincipal;
                } else {
                    log.warn("Object in cache is not of type User: {}", userInCache.getClass().getName());
                    // Xóa dữ liệu cache không hợp lệ
                    cacheService.delete(AppConstants.USERS_CACHE, idStr);
                }
            } else {
                log.debug("Không tìm thấy user trong cache");
            }
        } catch (Exception e) {
            log.error("Error loading user from cache by ID: {}", e.getMessage(), e);
            // Xóa dữ liệu cache có thể gây lỗi
            try {
                cacheService.delete(AppConstants.USERS_CACHE, idStr);
            } catch (Exception ex) {
                log.warn("Error deleting invalid cache: {}", ex.getMessage());
            }
        }

        // Nếu không lấy được từ cache, lấy từ database
        log.debug("Tìm user trong database với id: {}", id);
        user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy user với id: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        log.debug("Đã tìm thấy user trong database: id={}, username={}", user.getId(), user.getUsername());
                
        // Lưu vào cache
        try {
            log.debug("Lưu user vào cache với key id={}", idStr);
            cacheService.set(AppConstants.USERS_CACHE, idStr, user, 1, java.util.concurrent.TimeUnit.DAYS);
            log.debug("Lưu user vào cache với key username={}", user.getUsername());
            cacheService.set(AppConstants.USERS_CACHE, user.getUsername(), user, 1, java.util.concurrent.TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Error caching user: {}", e.getMessage(), e);
        }

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        log.debug("Đã tạo UserPrincipal từ database: id={}, class={}", 
                 userPrincipal.getId(), userPrincipal.getClass().getName());
        return userPrincipal;
    }
} 