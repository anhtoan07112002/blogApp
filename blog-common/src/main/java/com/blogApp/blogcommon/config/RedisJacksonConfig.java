package com.blogApp.blogcommon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Lớp cấu hình chung để các module khác có thể dễ dàng import
 * Chỉ cần import lớp này để có đầy đủ cấu hình Jackson cho cả HTTP và Redis
 */
@Configuration
@Import({RedisConfig.class})
public class RedisJacksonConfig {
    // Không cần thêm gì ở đây, chỉ cần import các lớp cấu hình
} 