package com.blogApp.blogpost.config;

import com.blogApp.blogcommon.config.CommonSecurityConfig;
import com.blogApp.blogcommon.security.TokenAuthenticationFilter;
import com.blogApp.blogcommon.security.TokenProvider;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogpost.security.PostUserDetailsServiceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
@RequiredArgsConstructor
@Import(CommonSecurityConfig.class)
public class PostSecurityConfig {

    private final TokenProvider tokenProvider;
    private final PostUserDetailsServiceAdapter userDetailsService;
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
                        // Cho phép truy cập công khai vào các API đọc bài viết
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/posts/*/view").permitAll()
                        .requestMatchers(HttpMethod.POST, "/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()
                        .requestMatchers("/posts/public/**", "/posts/{slug}").permitAll()
                        .requestMatchers("/categories/**", "/tags/**").permitAll()
                        .requestMatchers("/comments/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                        // API quản lý yêu cầu xác thực
                        .requestMatchers("/posts/admin/**").hasRole("ADMIN")
                        .requestMatchers("/posts/update/**").authenticated()
                        .requestMatchers("/comments/create/**", "/comments/update/**", "/comments/delete/**").authenticated()
                        .anyRequest().authenticated());

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

