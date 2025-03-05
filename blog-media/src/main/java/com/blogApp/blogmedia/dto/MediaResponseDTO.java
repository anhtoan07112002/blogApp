package com.blogApp.blogmedia.dto;

import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogcommon.enums.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO dùng để trả về kết quả upload file
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponseDTO {
    // Thông tin cơ bản
    private UUID id;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String fileExtension;
    private String storageBucket;
    private String publicUrl;
    private MediaFileType mediaFileType;
    private Boolean isPublic;
    private Boolean isDeleted;
    
    // Thông tin xử lý
    private ProcessingStatus processingStatus;
    private String processingError;
    
    // Thông tin kích thước
    private Integer width;
    private Integer height;
    private Long duration;
    private String thumbnailUrl;
    
    // Metadata
    private Map<String, String> metadata;
    
    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
