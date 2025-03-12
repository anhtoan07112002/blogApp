package com.blogApp.blogmedia.service.impl;

import com.blogApp.blogmedia.exception.MediaProcessingException;
import com.blogApp.blogmedia.service.interfaces.MinioService;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai giả lập của MinioService khi MinIO bị vô hiệu hóa
 * Lưu trữ tạm thời trong bộ nhớ, chỉ để phục vụ phát triển và kiểm thử
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "minio.enabled", havingValue = "false")
public class MinioServiceMock implements MinioService {
    
    @Value("${minio.bucket-name:mock-bucket}")
    private String bucketName;
    
    // Lưu trữ tạm thời file data trong bộ nhớ
    private final Map<String, byte[]> fileStore = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> metadataStore = new ConcurrentHashMap<>();
    
    public MinioServiceMock() {
        log.warn("MinIO bị vô hiệu hóa. Sử dụng triển khai giả lập trong bộ nhớ thay thế.");
    }

    @Override
    public String uploadFile(MultipartFile file, String objectName, String contentType, Map<String, String> metadata) {
        try {
            byte[] bytes = file.getBytes();
            fileStore.put(objectName, bytes);
            metadataStore.put(objectName, new HashMap<>(metadata));
            
            log.info("Mock storage: File uploaded to memory storage: {}", objectName);
            return "mock://" + objectName;
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectName, long size, String contentType, Map<String, String> metadata) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            
            buffer.flush();
            byte[] bytes = buffer.toByteArray();
            
            fileStore.put(objectName, bytes);
            metadataStore.put(objectName, new HashMap<>(metadata));
            
            log.info("Mock storage: File uploaded to memory storage: {}", objectName);
            return "mock://" + objectName;
        } catch (IOException e) {
            log.error("Error reading input stream: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public InputStream getFile(String objectName) {
        byte[] data = fileStore.get(objectName);
        if (data == null) {
            log.error("File not found in mock storage: {}", objectName);
            throw new RuntimeException("File not found: " + objectName);
        }
        
        log.info("Mock storage: File retrieved from memory storage: {}", objectName);
        return new ByteArrayInputStream(data);
    }
    
    @Override
    public StatObjectResponse getObjectInfo(String objectName) {
        if (!fileStore.containsKey(objectName)) {
            log.error("File not found in mock storage: {}", objectName);
            throw new RuntimeException("File not found: " + objectName);
        }
        
        log.info("Mock storage: Object info requested for {}", objectName);
        return null; // Không thể tạo StatObjectResponse thực tế trong môi trường giả lập
    }

    @Override
    public boolean deleteFile(String objectName) {
        fileStore.remove(objectName);
        metadataStore.remove(objectName);
        log.info("Mock storage: File removed from memory storage: {}", objectName);
        return true;
    }

    @Override
    public boolean doesObjectExist(String objectName) {
        boolean exists = fileStore.containsKey(objectName);
        log.info("Mock storage: File exists check for {}: {}", objectName, exists);
        return exists;
    }

    @Override
    public String generatePresignedUrl(String objectName, int expirySeconds) {
        if (!fileStore.containsKey(objectName)) {
            log.error("File not found in mock storage: {}", objectName);
            throw new RuntimeException("File not found: " + objectName);
        }
        
        String mockUrl = "mock://" + objectName + "?token=" + UUID.randomUUID() + "&expires=" + expirySeconds;
        log.info("Mock storage: Generated presigned URL for: {}", objectName);
        return mockUrl;
    }
    
    @Override
    public String getPublicUrl(String objectName) {
        if (!fileStore.containsKey(objectName)) {
            log.error("File not found in mock storage: {}", objectName);
            throw new RuntimeException("File not found: " + objectName);
        }
        
        String mockUrl = "mock://" + objectName;
        log.info("Mock storage: Generated public URL for: {}", objectName);
        return mockUrl;
    }
    
    @Override
    public String getBucketName() {
        return bucketName;
    }
} 