package com.blogApp.blogpost.config;

import com.blogApp.blogcommon.config.RedisJacksonConfig;
import com.blogApp.blogcommon.config.WebJacksonConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import cấu hình Jackson chung từ blog-common
 * Điều này đảm bảo rằng cấu hình Jackson trong blog-common được sử dụng
 * cho cả HTTP REST API và Redis.
 */
@Configuration
@Import({RedisJacksonConfig.class, WebJacksonConfig.class})
public class PostJacksonConfig {}
