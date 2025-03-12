package com.blogApp.blogauth.config;

import com.blogApp.blogcommon.security.CustomUserDetailsService;
import com.blogApp.blogcommon.security.TokenAuthenticationFilter;
import com.blogApp.blogcommon.security.TokenProvider;
import com.blogApp.blogcommon.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class AuthSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final TokenProvider tokenProvider;
    private final CacheService cacheService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, customUserDetailsService, cacheService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
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
                        // Public endpoints
                        .requestMatchers("/signup", "/login", "/refresh-token", "/forgot-password/**", "/test").permitAll()
                        // Swagger UI endpoints
                        .requestMatchers(
                            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**",
                            "/api/auth/swagger-ui/**", "/api/auth/swagger-ui.html", "/api/auth/v3/api-docs/**", "/api/auth/webjars/**",
                            "/swagger-resources/**", "/swagger-resources", "/swagger-resources/configuration/ui",
                            "/swagger-resources/configuration/security"
                        ).permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated());

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}