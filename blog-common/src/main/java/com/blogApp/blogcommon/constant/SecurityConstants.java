package com.blogApp.blogcommon.constant;

public class SecurityConstants {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/posts/public/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };
}
