package com.blogApp.blogmedia.config;

import com.blogApp.blogcommon.config.RedisJacksonConfig;
import com.blogApp.blogcommon.config.WebJacksonConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import cấu hình Jackson chung từ blog-common
 * Điều này đảm bảo rằng cấu hình Jackson trong blog-common được sử dụng
 * cho cả HTTP và Redis.
 *
 * Lưu ý: Mỗi loại cấu hình (HTTP và Redis) sử dụng ObjectMapper riêng biệt
 * để tránh xung đột. RedisJacksonConfig sử dụng ObjectMapper với activateDefaultTyping
 * trong khi WebJacksonConfig sử dụng ObjectMapper không có tính năng này.
 */
@Configuration
@Import({RedisJacksonConfig.class, WebJacksonConfig.class})
public class MediaJacksonConfig {
    // Không cần thêm gì ở đây, chỉ cần import các cấu hình cần thiết
}