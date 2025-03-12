package com.blogApp.blogauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.blogApp.blogcommon", "com.blogApp.blogauth"})
@EntityScan(basePackages = {"com.blogApp.blogcommon.model", "com.blogApp.blogauth.model"})
@EnableJpaRepositories(basePackages = {"com.blogApp.blogauth.repository"})
public class BlogAuthApplication {
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BlogAuthApplication.class, args);

        String port = context.getEnvironment().getProperty("server.port");
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
        String appName = context.getEnvironment().getProperty("spring.application.name");

        System.out.println("---------------------------------------------");
        System.out.println("|                                           |");
        System.out.println("|     🚀 Auth Service khởi động thành công 🚀     |");
        System.out.println("|                                           |");
        System.out.println("---------------------------------------------");
        System.out.println("❄ Tên ứng dụng: " + appName);
        System.out.println("🌐 Địa chỉ: http://localhost:" + port + contextPath);
        System.out.println("📄 Swagger: http://localhost:" + port + contextPath + "/swagger-ui/index.html");
        System.out.println("📊 Actuator: http://localhost:" + port + contextPath + "/actuator");
        System.out.println("---------------------------------------------");
    }
} 