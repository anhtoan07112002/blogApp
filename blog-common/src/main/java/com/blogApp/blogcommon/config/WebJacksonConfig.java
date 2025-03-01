package com.blogApp.blogcommon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Cấu hình chung cho web
 * Các module khác có thể import lớp này để sử dụng tất cả các cấu hình web chung
 */
@Configuration
@Import({JacksonHttpConfig.class, WebMvcConfig.class})
public class WebJacksonConfig {
    // Không cần thêm gì ở đây, chỉ cần import các cấu hình cần thiết
} 