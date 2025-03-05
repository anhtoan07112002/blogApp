package com.blogApp.blogmedia.dto;

import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.enums.MediaFileType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO cho việc upload media
 * Sử dụng để nhận dữ liệu từ client khi upload file
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadDTO {
    
    /**
     * Loại media (IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER)
     */
    @NotNull(message = "Loại media không được để trống")
    private MediaFileType mediaFileType;
    
    /**
     * Cho phép truy cập công khai hay không
     */
    private Boolean isPublic = false;
    
    /**
     * Metadata bổ sung cho file
     * Ví dụ: 
     * - width, height cho ảnh/video
     * - duration cho video/audio
     * - description, tags, etc.
     */
    private Map<String, String> metadata;
    
    /**
     * ID của user upload file
     */
    private String userId;
    
    /**
     * Tên file gốc
     */
    private String originalFileName;
    
    /**
     * Loại nội dung của file (MIME type)
     */
    @Pattern(regexp = "^(image|video|audio|application)/.*", message = "Loại file không được hỗ trợ")
    private String contentType;

    /**
     * Kích thước file (bytes)
     */
    @Size(max = (int) AppConstants.MAX_FILE_SIZE, message = "Kích thước file không được vượt quá " + AppConstants.MAX_FILE_SIZE / (1024 * 1024) + "MB")
    private Long fileSize;
    
    /**
     * Đường dẫn lưu trữ file
     */
    private String filePath;
    
    /**
     * Phần mở rộng của file
     */
    private String fileExtension;
    
    /**
     * Bucket lưu trữ file
     */
    private String storageBucket;
}
