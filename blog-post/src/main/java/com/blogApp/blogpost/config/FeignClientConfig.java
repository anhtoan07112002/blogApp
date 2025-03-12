package com.blogApp.blogpost.config;

import com.blogApp.blogcommon.constant.SecurityConstants;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Cấu hình cho Feign Client
 * - Thêm interceptor để chuyển tiếp headers giữa các service
 * - Đảm bảo các thông tin xác thực được giữ nguyên
 */
@Configuration
public class FeignClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(FeignClientConfig.class);

    /**
     * Tạo interceptor để chuyển tiếp Authorization header
     * - Lấy header từ request hiện tại
     * - Thêm vào request của Feign Client
     */
//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
//            try {
//                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//                String token = null;
//
////                if (authentication instanceof JwtAuthenticationToken jwtToken) {
////                    String tokenValue = jwtToken.getToken().getTokenValue();
////                    if (tokenValue != null && !tokenValue.isEmpty()) {
////                        requestTemplate.header("Authorization", "Bearer " + tokenValue);
////                    } else {
////                        logger.warn("Token value is empty");
////                    }
////                } else {
////                    logger.warn("Authentication is not a JwtAuthenticationToken: {}",
////                        authentication != null ? authentication.getClass().getName() : "null");
////                }
////            } catch (Exception e) {
////                logger.error("Error while setting authorization header", e);
////            }
////        };
//                if (authentication != null) {
//                    Object credentials = authentication.getCredentials();
//                    if (credentials != null) {
//                        token = credentials.toString();
//                    } else {
//                        // Thử lấy token từ request context
//                        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//                        if (requestAttributes != null) {
//                            HttpServletRequest request = requestAttributes.getRequest();
//                            String authHeader = request.getHeader(SecurityConstants.HEADER_STRING);
//                            if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
//                                token = authHeader.substring(7);
//                            }
//                        }
//                    }
//                }
//
//                if (token != null && !token.isEmpty()) {
//                    requestTemplate.header("Authorization", "Bearer " + token);
//                    logger.debug("Added Authorization token to Feign request");
//                } else {
//                    logger.warn("No token found to forward to Auth Service");
//                }
//            } catch (Exception e) {
//                logger.error("Error while setting authorization header", e);
//            }
//        };
//    }
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            try {
                // Get the current request context
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    HttpServletRequest request = requestAttributes.getRequest();
                    String authHeader = request.getHeader(SecurityConstants.HEADER_STRING);
                    if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                        // Forward the exact same Authorization header
                        requestTemplate.header(SecurityConstants.HEADER_STRING, authHeader);
                        logger.debug("Successfully forwarded Authorization header to Auth Service");
                        return;
                    }
                }

                // If we couldn't get token from request context, log a warning
                logger.warn("No token found in request context to forward to Auth Service");

            } catch (Exception e) {
                logger.error("Error while setting authorization header", e);
            }
        };
    }
}
