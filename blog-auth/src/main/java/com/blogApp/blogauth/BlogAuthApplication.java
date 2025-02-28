package com.blogApp.blogauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.blogApp.blogcommon", "com.blogApp.blogauth"})
@EntityScan(basePackages = {"com.blogApp.blogcommon.model", "com.blogApp.blogauth.model"})
@EnableJpaRepositories(basePackages = {"com.blogApp.blogauth.repository"})
public class BlogAuthApplication {
    
    public static void main(String[] args) {
        log.info("Starting BlogAuthApplication...");
        SpringApplication.run(BlogAuthApplication.class, args);
        log.info("BlogAuthApplication started successfully!");
    }
} 