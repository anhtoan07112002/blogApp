package com.blogApp.blogcommon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Cấu hình WebMvc chung cho tất cả các module
 * Đảm bảo rằng các controller sử dụng đúng ObjectMapper cho HTTP
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectMapper httpObjectMapper;

    public WebMvcConfig(@Qualifier("httpObjectMapper") ObjectMapper httpObjectMapper) {
        this.httpObjectMapper = httpObjectMapper;
    }

    /**
     * Cấu hình các HTTP message converter
     * Đảm bảo rằng MappingJackson2HttpMessageConverter sử dụng httpObjectMapper
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Xóa các MappingJackson2HttpMessageConverter mặc định
        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        
        // Thêm converter mới với httpObjectMapper
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(httpObjectMapper);
        converters.add(converter);
    }
} 