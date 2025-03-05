package com.blogApp.blogmedia.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình MinIO cho Media Service
 * - Khởi tạo MinIO client
 * - Tự động tạo bucket nếu chưa tồn tại
 * - Cấu hình endpoint và credentials
 * - Xử lý lỗi và logging
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.region}")
    private String region;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.secure:false}")
    private boolean secure;
    
    @Value("${minio.create-bucket-if-not-exists:true}")
    private boolean createBucketIfNotExists;

    /**
     * Khởi tạo MinIO client với các cấu hình từ application.yml
     * - Tự động tạo bucket nếu chưa tồn tại
     * - Xử lý lỗi và logging
     * - Hỗ trợ HTTPS nếu secure=true
     */
    @Bean
    public MinioClient minioClient() {
        try {
            // Xử lý URL dựa trên secure flag
            String endpoint = secure ? 
                minioUrl.replace("http://", "https://") : 
                minioUrl.replace("https://", "http://");
            
            log.info("Initializing MinIO client with URL: {}", endpoint);
            
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .region("vietnam")
                    .build();
            
            // Kiểm tra và tạo bucket nếu cần
            if (createBucketIfNotExists) {
                boolean bucketExists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucketName).build()
                );
                
                if (!bucketExists) {
                    log.info("Creating bucket: {}", bucketName);
                    minioClient.makeBucket(
                            MakeBucketArgs.builder().bucket(bucketName).build()
                    );
                    log.info("Bucket created successfully: {}", bucketName);
                } else {
                    log.info("Bucket already exists: {}", bucketName);
                }
            }
            
            log.info("MinIO client initialized successfully");
            return minioClient;
            
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client: {}", e.getMessage(), e);
            throw new RuntimeException("Could not initialize MinIO client", e);
        }
    }
}