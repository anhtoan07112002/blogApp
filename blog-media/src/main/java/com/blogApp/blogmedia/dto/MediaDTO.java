package com.blogApp.blogmedia.dto;

import com.blogApp.blogcommon.enums.MediaFileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO chứa thông tin media
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết của một file media")
public class MediaDTO {
    @Schema(description = "ID của media", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Tên file gốc", example = "example-image.jpg")
    private String fileName;
    
    @Schema(description = "MIME type của file", example = "image/jpeg")
    private String contentType;
    
    @Schema(description = "Kích thước file (bytes)", example = "1048576")
    private Long size;
    
    @Schema(description = "Loại file media", example = "IMAGE")
    private MediaFileType type;
    
    @Schema(description = "URL truy cập file", example = "http://example.com/media/image.jpg")
    private String url;
    
    @Schema(description = "Metadata bổ sung của file")
    private Map<String, String> metadata;
    
    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời điểm cập nhật gần nhất")
    private LocalDateTime updatedAt;
}
