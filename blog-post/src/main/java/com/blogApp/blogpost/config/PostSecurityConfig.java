package com.blogApp.blogpost.config;

import com.blogApp.blogcommon.config.CommonSecurityConfig;
import com.blogApp.blogcommon.security.CustomUserDetailsService;
import com.blogApp.blogcommon.security.TokenAuthenticationFilter;
import com.blogApp.blogcommon.security.TokenProvider;
import com.blogApp.blogcommon.service.CacheService;
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
 * Cấu hình bảo mật cho Post Service
 * - Cấu hình xác thực và phân quyền
 * - Bảo vệ các endpoint API
 * - Cấu hình CORS và CSRF
 * - Sử dụng JWT để xác thực
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Import(CommonSecurityConfig.class)
public class PostSecurityConfig {

    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final CacheService cacheService;

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, customUserDetailsService, cacheService);
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
                        // Cho phép truy cập công khai vào các API đọc bài viết
                        .requestMatchers("/posts/public/**", "/posts", "/posts/{slug}").permitAll()
                        .requestMatchers("/categories/**", "/tags/**").permitAll()
                        .requestMatchers("/comments/public/**").permitAll()
                        // API quản lý yêu cầu xác thực
                        .requestMatchers("/posts/admin/**").hasRole("ADMIN")
                        .requestMatchers("/posts/create", "/posts/update/**", "/posts/delete/**").authenticated()
                        .requestMatchers("/comments/create/**", "/comments/update/**", "/comments/delete/**").authenticated()
                        .anyRequest().authenticated());

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

