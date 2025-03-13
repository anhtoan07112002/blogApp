package com.blogApp.blogmedia.config;

import com.blogApp.blogcommon.config.CommonSecurityConfig;
import com.blogApp.blogcommon.security.TokenAuthenticationFilter;
import com.blogApp.blogcommon.security.TokenProvider;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogmedia.security.MediaUserDetailsServiceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình bảo mật cho Media Service
 * - Cấu hình xác thực và phân quyền
 * - Bảo vệ các endpoint API
 * - Cấu hình CORS và CSRF
 * - Sử dụng JWT để xác thực
 * 
 * Các endpoint được phân quyền:
 * - /media/public/** : Truy cập công khai (xem media)
 * - /media/admin/** : Yêu cầu role ADMIN (quản lý media)
 * - /media/upload/** : Yêu cầu xác thực (upload media)
 * - /media/update/** : Yêu cầu xác thực (cập nhật media)
 * - /media/delete/** : Yêu cầu xác thực (xóa media)
 * - /media/process/** : Yêu cầu xác thực (xử lý media)
 * - /media/thumbnail/** : Yêu cầu xác thực (tạo thumbnail)
 * - /media/resize/** : Yêu cầu xác thực (resize ảnh)
 * - /media/convert/** : Yêu cầu xác thực (chuyển đổi định dạng)
 * - /media/compress/** : Yêu cầu xác thực (nén media)
 * - /media/watermark/** : Yêu cầu xác thực (thêm watermark)
 * - /media/metadata/** : Yêu cầu xác thực (lấy metadata)
 * - /media/search/** : Yêu cầu xác thực (tìm kiếm media)
 * - /media/batch/** : Yêu cầu xác thực (xử lý hàng loạt)
 * - /media/backup/** : Yêu cầu xác thực (backup media)
 * - /media/restore/** : Yêu cầu xác thực (khôi phục media)
 * - /media/cleanup/** : Yêu cầu xác thực (dọn dẹp media)
 * - /media/stats/** : Yêu cầu xác thực (thống kê media)
 * - /media/health/** : Yêu cầu xác thực (kiểm tra sức khỏe)
 * - /media/metrics/** : Yêu cầu xác thực (metrics)
 * - /media/audit/** : Yêu cầu xác thực (audit log)
 * - /media/config/** : Yêu cầu xác thực (cấu hình)
 * - /media/test/** : Yêu cầu xác thực (test)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Import(CommonSecurityConfig.class)
public class MediaSecurityConfig {

    private final TokenProvider tokenProvider;
    private final MediaUserDetailsServiceAdapter userDetailsService;
    private final CacheService cacheService;
    
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, userDetailsService, cacheService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập công khai vào các API đọc media
                        .requestMatchers("/api/v1/media/*/content", "/api/v1/media/*/url").permitAll()
                        
                        // Cho phép truy cập vào tất cả các endpoint Swagger
                        .requestMatchers(
                            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**",
                            contextPath + "/swagger-ui/**", contextPath + "/swagger-ui.html", 
                            contextPath + "/v3/api-docs/**", contextPath + "/webjars/**",
                            "/swagger-resources/**", "/swagger-resources", 
                            "/swagger-resources/configuration/ui",
                            "/swagger-resources/configuration/security"
                        ).permitAll()
                        
                        // API upload và quản lý media yêu cầu xác thực
                        .requestMatchers("/api/v1/media/upload/**", "/api/v1/media/upload-with-metadata").authenticated()
                        .requestMatchers("/api/v1/media/post/**").authenticated()
                        
                        // Các request khác yêu cầu xác thực
                        .anyRequest().authenticated());

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}