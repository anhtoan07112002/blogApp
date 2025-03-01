package com.blogApp.blogcommon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

/**
 * Cấu hình Jackson chung cho HTTP request/response trong tất cả các module
 * Cấu hình này sẽ ghi đè cấu hình mặc định của Spring Boot cho HTTP
 * nhưng không ảnh hưởng đến cấu hình Redis
 */
@Configuration
public class JacksonHttpConfig {

    /**
     * Tạo ObjectMapper chính cho HTTP request/response
     * Đánh dấu @Primary để được ưu tiên sử dụng cho HTTP
     */
    @Bean
    @Primary
    @Qualifier("httpObjectMapper")
    public ObjectMapper httpObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Tắt tính năng yêu cầu thông tin kiểu đối tượng trong JSON
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        
        // Hỗ trợ Java 8 date/time
        mapper.registerModule(new JavaTimeModule());
        
        return mapper;
    }
    
    /**
     * Tạo HTTP message converter sử dụng ObjectMapper đã cấu hình
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(@Qualifier("httpObjectMapper") ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
    
    /**
     * Tùy chỉnh Jackson2ObjectMapperBuilder mặc định của Spring Boot
     * Điều này sẽ ảnh hưởng đến tất cả các ObjectMapper được tạo bởi Spring Boot
     * trừ khi chúng được cấu hình riêng
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonBuilderCustomizer() {
        return builder -> {
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        };
    }
} 