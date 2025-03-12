package com.blogApp.blogpost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
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
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class BlogPostApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BlogPostApplication.class, args);

        String port = context.getEnvironment().getProperty("server.port");
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
        String appName = context.getEnvironment().getProperty("spring.application.name");

        System.out.println("---------------------------------------------");
        System.out.println("|                                           |");
        System.out.println("|     🚀 Posts Service khởi động thành công 🚀     |");
        System.out.println("|                                           |");
        System.out.println("---------------------------------------------");
        System.out.println("❄ Tên ứng dụng: " + appName);
        System.out.println("🌐 Địa chỉ: http://localhost:" + port + contextPath);
        System.out.println("📄 Swagger: http://localhost:" + port + contextPath + "/swagger-ui/index.html");
        System.out.println("📊 Actuator: http://localhost:" + port + contextPath + "/actuator");
        System.out.println("---------------------------------------------");
    }
}