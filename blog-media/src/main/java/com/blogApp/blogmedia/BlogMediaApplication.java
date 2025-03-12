package com.blogApp.blogmedia;

import com.blogApp.blogcommon.config.CommonSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * Lớp chính khởi chạy ứng dụng Blog Media Service
 * - Quản lý media cho blog (hình ảnh, video, tài liệu...)
 * - Cung cấp API xử lý và lưu trữ media
 * - Tích hợp với MinIO để lưu trữ file
 * - Tích hợp với Auth Service để xác thực và phân quyền
 */
@SpringBootApplication(scanBasePackages = {"com.blogApp.blogmedia", "com.blogApp.blogcommon"})
@EnableFeignClients
@Import(CommonSecurityConfig.class)
public class BlogMediaApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BlogMediaApplication.class, args);
        
        String port = context.getEnvironment().getProperty("server.port");
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
        String appName = context.getEnvironment().getProperty("spring.application.name");
        
        System.out.println("---------------------------------------------");
        System.out.println("|                                           |");
        System.out.println("|     🚀 Media Service khởi động thành công 🚀     |");
        System.out.println("|                                           |");
        System.out.println("---------------------------------------------");
        System.out.println("❄ Tên ứng dụng: " + appName);
        System.out.println("🌐 Địa chỉ: http://localhost:" + port + contextPath);
        System.out.println("📄 Swagger: http://localhost:" + port + contextPath + "/swagger-ui/index.html");
        System.out.println("📊 Actuator: http://localhost:" + port + contextPath + "/actuator");
        System.out.println("---------------------------------------------");
    }
} 