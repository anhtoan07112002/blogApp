package com.blogApp.blogcommon.constant;

public class SecurityConstants {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/posts/public/**",
            "/api/categories/**",
            "/api/tags/**",
            "/api/comments/public/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/v3/api-docs/swagger-config",
            "/webjars/**",
            "/api/*/swagger-ui/**",
            "/api/*/swagger-ui.html",
            "/api/*/v3/api-docs/**",
            "/api/*/v3/api-docs/swagger-config",
            "/api/*/webjars/**"
    };
}
