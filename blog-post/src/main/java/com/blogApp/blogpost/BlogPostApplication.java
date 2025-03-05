package com.blogApp.blogpost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import lombok.extern.slf4j.Slf4j;

/**
 * Blog Post Application
 * 
 * @author Bui Anh Toan
 * @version 1.0
 * @since 2024-03-03
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.blogApp.blogcommon", "com.blogApp.blogpost"})
@EntityScan(basePackages = {"com.blogApp.blogcommon.model", "com.blogApp.blogpost.model"})
@EnableJpaRepositories(basePackages = {"com.blogApp.blogpost.repository"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class BlogPostApplication {

    public static void main(String[] args) {
        log.info("Đang khởi động Blog Post Service...");
        try {
            SpringApplication.run(BlogPostApplication.class, args);
            log.info("Blog Post Service đã khởi động thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi khởi động Blog Post Service: {}", e.getMessage(), e);
            throw e;
        }
    }
}