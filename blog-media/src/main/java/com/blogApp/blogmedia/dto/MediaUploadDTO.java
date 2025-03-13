package com.blogApp.blogmedia.dto;

import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.enums.MediaFileType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin yêu cầu upload file media")
public class MediaUploadDTO {
    
    /**
     * Loại media (IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER)
     */
    @NotNull(message = "Loại media không được để trống")
    @Schema(description = "Loại media", example = "IMAGE", requiredMode = Schema.RequiredMode.REQUIRED)
    private MediaFileType mediaFileType;
    
    /**
     * Cho phép truy cập công khai hay không
     */
    @Schema(description = "Cho phép truy cập công khai không", example = "false", defaultValue = "false")
    private Boolean isPublic = false;
    
    /**
     * Metadata bổ sung cho file
     * Ví dụ: 
     * - width, height cho ảnh/video
     * - duration cho video/audio
     * - description, tags, etc.
     */
    @Schema(description = "Metadata bổ sung cho file")
    private Map<String, String> metadata;
    
    /**
     * ID của user upload file
     */
    @Schema(description = "ID của người dùng upload file", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;
    
    /**
     * Tên file gốc
     */
    @Schema(description = "Tên file gốc", example = "my-image.jpg")
    private String originalFileName;
    
    /**
     * Loại nội dung của file (MIME type)
     */
    @Pattern(regexp = "^(image|video|audio|application)/.*", message = "Loại file không được hỗ trợ")
    @Schema(description = "MIME type của file", example = "image/jpeg")
    private String contentType;

    /**
     * Kích thước file (bytes)
     */
    @Size(max = (int) AppConstants.MAX_FILE_SIZE, message = "Kích thước file không được vượt quá " + AppConstants.MAX_FILE_SIZE / (1024 * 1024) + "MB")
    @Schema(description = "Kích thước file (bytes)", example = "1048576")
    private Long fileSize;
    
    /**
     * Đường dẫn lưu trữ file
     */
    @Schema(description = "Đường dẫn lưu trữ file trong hệ thống", example = "images/2023/04/my-image.jpg")
    private String filePath;
    
    /**
     * Phần mở rộng của file
     */
    @Schema(description = "Phần mở rộng của file", example = "jpg")
    private String fileExtension;
    
    /**
     * Bucket lưu trữ file
     */
    @Schema(description = "Bucket lưu trữ file", example = "media-bucket")
    private String storageBucket;
}
