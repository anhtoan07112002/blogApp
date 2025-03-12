package com.blogApp.blogmedia.service.impl;

import com.blogApp.blogcommon.constant.ErrorCodes;
import com.blogApp.blogcommon.exception.BlogException;
import com.blogApp.blogmedia.exception.MediaProcessingException;
import com.blogApp.blogmedia.exception.MediaUploadException;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogmedia.service.interfaces.MinioService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation cho việc tương tác với MinIO storage
 * Xử lý các thao tác upload, download, xóa file và các thao tác khác với MinIO
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = true)
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${minio.url}")
    private String minioUrl;
   
    @Override
    public String uploadFile(MultipartFile file, String objectName, String contentType, Map<String, String> metadata) {
        try {
            InputStream inputStream = file.getInputStream();
            return uploadFile(inputStream, objectName, file.getSize(), contentType, metadata);
        } catch (IOException e) {
            log.error("Lỗi đọc file input stream: {}", e.getMessage(), e);
            throw new MediaUploadException("Không thể đọc file input", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectName, long size, String contentType, Map<String, String> metadata) {
        try {
            // Tạo headers với content type và user metadata
            Map<String, String> headers = new HashMap<>();
            if (contentType != null && !contentType.isEmpty()) {
                headers.put("Content-Type", contentType);
            }
            
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .headers(headers);
            
            // Thêm metadata nếu có
            if (metadata != null && !metadata.isEmpty()) {
                builder.userMetadata(metadata);
            }
            
            // Thực hiện upload
            minioClient.putObject(builder.build());
            log.info("File được upload thành công: {}", objectName);
            
            return getPublicUrl(objectName);
        } catch (Exception e) {
            log.error("Lỗi upload file lên MinIO: {}", e.getMessage(), e);
            throw new MediaUploadException("Lỗi upload file lên storage", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("Không thể đóng input stream", e);
            }
        }
    }

    @Override
    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                log.warn("Không tìm thấy file: {}", objectName);
                throw new ResourceNotFoundException("File không tồn tại: " + objectName);
            }
            log.error("Lỗi lấy file từ MinIO: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể lấy file", e);
        } catch (Exception e) {
            log.error("Lỗi lấy file từ MinIO: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể lấy file", e);
        }
    }

    @Override
    public StatObjectResponse getObjectInfo(String objectName) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                log.warn("Không tìm thấy file: {}", objectName);
                throw new ResourceNotFoundException("File không tồn tại: " + objectName);
            }
            log.error("Lỗi lấy thông tin object từ MinIO: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể lấy thông tin object", e);
        } catch (Exception e) {
            log.error("Lỗi lấy thông tin object từ MinIO: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể lấy thông tin object", e);
        }
    }

    @Override
    public boolean deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("File đã được xóa thành công: {}", objectName);
            return true;
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                log.warn("Không tìm thấy file để xóa: {}", objectName);
                throw new ResourceNotFoundException("File không tồn tại: " + objectName);
            }
            log.error("Lỗi xóa file từ MinIO: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể xóa file", e);
        } catch (Exception e) {
            log.error("Lỗi xóa file từ MinIO: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể xóa file", e);
        }
    }

    @Override
    public boolean doesObjectExist(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                return false;
            }
            log.error("Lỗi kiểm tra file tồn tại trong MinIO: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể kiểm tra file tồn tại", e);
        } catch (Exception e) {
            log.error("Lỗi kiểm tra file tồn tại trong MinIO: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể kiểm tra file tồn tại", e);
        }
    }

    @Override
    public String generatePresignedUrl(String objectName, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Lỗi tạo presigned URL: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể tạo presigned URL", e);
        }
    }

    @Override
    public String getPublicUrl(String objectName) {
        return String.format("%s/%s/%s", minioUrl, bucketName, objectName);
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }
}
