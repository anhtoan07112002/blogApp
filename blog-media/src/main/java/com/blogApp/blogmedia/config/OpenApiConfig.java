package com.blogApp.blogmedia.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

import org.springdoc.core.models.GroupedOpenApi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Blog Media API")
                        .description("API cho dịch vụ quản lý media (hình ảnh, video, tài liệu) của ứng dụng Blog")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Blog Team")
                                .email("contact@blog.com")
                                .url("https://blog.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083" + contextPath)
                                .description("Local Development Server"),
                        new Server()
                                .url("http://prod-server:8083" + contextPath)
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token Authentication. Nhập token không có tiền tố 'Bearer '.")))
                .tags(List.of(
                        new Tag().name("Media").description("API quản lý tệp media như hình ảnh, video, âm thanh, tài liệu"),
                        new Tag().name("Media-Post").description("API quản lý liên kết giữa media và bài viết"),
                        new Tag().name("Media-Upload").description("API upload và xử lý media")
                ));
    }
} 