package com.blogApp.blogmedia.config;

import com.blogApp.blogcommon.config.CommonSecurityConfig;
import com.blogApp.blogcommon.security.TokenAuthenticationFilter;
import com.blogApp.blogcommon.security.TokenProvider;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogmedia.security.MediaUserDetailsServiceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
@RequiredArgsConstructor
@Import(CommonSecurityConfig.class)
public class MediaSecurityConfig {

    private final TokenProvider tokenProvider;
    private final MediaUserDetailsServiceAdapter userDetailsService;
    private final CacheService cacheService;

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
                        .requestMatchers("/media/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                        
                        // API quản lý yêu cầu role ADMIN
                        .requestMatchers("/media/admin/**").hasRole("ADMIN")
                        
                        // API upload media yêu cầu xác thực
                        .requestMatchers("/media/upload/**").authenticated()
                        
                        // API cập nhật media yêu cầu xác thực
                        .requestMatchers("/media/update/**").authenticated()
                        
                        // API xóa media yêu cầu xác thực
                        .requestMatchers("/media/delete/**").authenticated()
                        
                        // API xử lý media yêu cầu xác thực
                        .requestMatchers("/media/process/**").authenticated()
                        
                        // API tạo thumbnail yêu cầu xác thực
                        .requestMatchers("/media/thumbnail/**").authenticated()
                        
                        // API resize ảnh yêu cầu xác thực
                        .requestMatchers("/media/resize/**").authenticated()
                        
                        // API chuyển đổi định dạng yêu cầu xác thực
                        .requestMatchers("/media/convert/**").authenticated()
                        
                        // API nén media yêu cầu xác thực
                        .requestMatchers("/media/compress/**").authenticated()
                        
                        // API thêm watermark yêu cầu xác thực
                        .requestMatchers("/media/watermark/**").authenticated()
                        
                        // API lấy metadata yêu cầu xác thực
                        .requestMatchers("/media/metadata/**").authenticated()
                        
                        // API tìm kiếm media yêu cầu xác thực
                        .requestMatchers("/media/search/**").authenticated()
                        
                        // API xử lý hàng loạt yêu cầu xác thực
                        .requestMatchers("/media/batch/**").authenticated()
                        
                        // API backup media yêu cầu xác thực
                        .requestMatchers("/media/backup/**").authenticated()
                        
                        // API khôi phục media yêu cầu xác thực
                        .requestMatchers("/media/restore/**").authenticated()
                        
                        // API dọn dẹp media yêu cầu xác thực
                        .requestMatchers("/media/cleanup/**").authenticated()
                        
                        // API thống kê media yêu cầu xác thực
                        .requestMatchers("/media/stats/**").authenticated()
                        
                        // API kiểm tra sức khỏe yêu cầu xác thực
                        .requestMatchers("/media/health/**").authenticated()
                        
                        // API metrics yêu cầu xác thực
                        .requestMatchers("/media/metrics/**").authenticated()
                        
                        // API audit log yêu cầu xác thực
                        .requestMatchers("/media/audit/**").authenticated()
                        
                        // API cấu hình yêu cầu xác thực
                        .requestMatchers("/media/config/**").authenticated()
                        
                        // API test yêu cầu xác thực
                        .requestMatchers("/media/test/**").authenticated()
                        
                        // Các request khác yêu cầu xác thực
                        .anyRequest().authenticated());

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}