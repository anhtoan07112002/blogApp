package com.blogApp.blogcommon.util;

import com.blogApp.blogcommon.dto.response.UserPrincipal;
import com.blogApp.blogcommon.exception.BlogException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BlogException("User not authenticated", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}
