package com.blogApp.blogcommon.security;

import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.constant.SecurityConstants;
import com.blogApp.blogcommon.dto.response.UserPrincipal;
import com.blogApp.blogcommon.service.CacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CacheService cacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            String requestURI = request.getRequestURI();
            
            log.debug("TokenAuthenticationFilter xử lý request: {} {}", request.getMethod(), requestURI);

            if (StringUtils.hasText(token)) {
                log.debug("Token được tìm thấy trong request");
                
                // Kiểm tra xem token có trong BlackList không
                if (cacheService.hasKey(AppConstants.ACCESS_TOKEN_BLACKLIST_CACHE, token)) {
                    log.warn("Phát hiện token đã bị vô hiệu hóa (đã đăng xuất)");
                    SecurityContextHolder.clearContext();
                } else {
                    // Token hợp lệ, tiếp tục xử lý
                    log.debug("Token hợp lệ, đang xác thực");
                    Long userId = tokenProvider.getUserIdFromToken(token);
                    log.debug("UserId từ token: {}", userId);
                    
                    try {
                        UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserById(userId);
                        log.debug("Đã lấy thông tin UserPrincipal: id={}, username={}, authorities={}",
                                userDetails.getId(), userDetails.getUsername(), userDetails.getAuthorities());
                        
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        log.debug("Thiết lập xác thực vào SecurityContext");
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (ClassCastException e) {
                        log.error("Lỗi ép kiểu khi lấy UserPrincipal: {}", e.getMessage(), e);
                        SecurityContextHolder.clearContext();
                    }
                }
            } else {
                log.debug("Không tìm thấy token trong request hoặc token không hợp lệ");
            }
        } catch (Exception ex) {
            log.error("Không thể thiết lập xác thực người dùng trong security context: {}", ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }

        log.debug("TokenAuthenticationFilter hoàn tất, chuyển tiếp request đến filter tiếp theo");
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            String token = bearerToken.substring(7);
            log.debug("Trích xuất token từ request: {} (hiển thị 10 ký tự đầu...)", 
                     token.length() > 10 ? token.substring(0, 10) + "..." : token);
            return token;
        }
        return null;
    }
}
