package com.blogApp.blogmedia.dto;

import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogcommon.enums.ProcessingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin chi tiết media cho response")
public class MediaResponseDTO {
    // Thông tin cơ bản
    @Schema(description = "ID của media", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Tên file gốc", example = "vacation-photo.jpg")
    private String fileName;
    
    @Schema(description = "Tên file đã xử lý", example = "550e8400-e29b-41d4-a716-446655440000.jpg")
    private String originalFileName;
    
    @Schema(description = "Đường dẫn lưu trữ file", example = "images/2023/04/image.jpg")
    private String filePath;
    
    @Schema(description = "Kích thước file (bytes)", example = "1048576")
    private Long fileSize;
    
    @Schema(description = "MIME type của file", example = "image/jpeg")
    private String contentType;
    
    @Schema(description = "Phần mở rộng của file", example = "jpg")
    private String fileExtension;
    
    @Schema(description = "Loại file media", example = "IMAGE")
    private MediaFileType mediaFileType;
    
    @Schema(description = "Là file công khai hay không", example = "true")
    private Boolean isPublic;
    
    @Schema(description = "Trạng thái xóa của file", example = "false")
    private Boolean isDeleted;
    
    @Schema(description = "Bucket lưu trữ file", example = "media-bucket")
    private String storageBucket;
    
    @Schema(description = "URL truy cập file", example = "http://example.com/media/image.jpg")
    private String publicUrl;
    
    // Thông tin xử lý
    @Schema(description = "Trạng thái xử lý")
    private ProcessingStatus processingStatus;
    
    @Schema(description = "Lỗi xử lý")
    private String processingError;
    
    // Thông tin kích thước
    @Schema(description = "Chiều rộng (px) - cho ảnh và video", example = "1920")
    private Integer width;
    
    @Schema(description = "Chiều cao (px) - cho ảnh và video", example = "1080")
    private Integer height;
    
    @Schema(description = "Thời lượng (ms) - cho audio và video", example = "120000")
    private Long duration;
    
    @Schema(description = "URL thumbnail của file")
    private String thumbnailUrl;
    
    // Metadata
    @Schema(description = "Metadata bổ sung của file")
    private Map<String, String> metadata;
    
    // Thời gian
    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời điểm cập nhật gần nhất")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Thời điểm xóa, null nếu chưa xóa")
    private LocalDateTime deletedAt;
}
