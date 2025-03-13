package com.blogApp.blogmedia.model;

import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogcommon.enums.ProcessingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * Media model
 * Sử dụng UUID làm khóa chính
 * Tự động quản lý thời gian tạo/cập nhật
 * Đảm bảo file_name là duy nhất
 * Đảm bảo file_path là duy nhất
 * Đảm bảo file_extension là duy nhất
 * Đảm bảo storage_bucket là duy nhất
 * Đảm bảo public_url là duy nhất
 * Đảm bảo user_id là duy nhất
 * Đảm bảo is_public là duy nhất
 */
@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin một tệp media được lưu trữ trong hệ thống")
public class Media {
    /**
     * ID của media
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "ID duy nhất của media", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    /**
     * Tên file
     */
    @Column(name = "file_name")
    @Schema(description = "Tên file đã được xử lý để lưu trữ", example = "550e8400-e29b-41d4-a716-446655440000.jpg")
    private String fileName;
    
    /**
     * Tên file gốc
     */
    @Column(name = "original_file_name")
    @Schema(description = "Tên file gốc từ người dùng", example = "family-photo.jpg")
    private String originalFileName;
    
    /**
     * Đường dẫn file
     */
    @Column(name = "file_path")
    @Schema(description = "Đường dẫn đầy đủ đến file", example = "images/2023/04/550e8400-e29b-41d4-a716-446655440000.jpg")
    private String filePath;
    
    /**
     * Kích thước file
     */
    @Column(name = "file_size")
    @Schema(description = "Kích thước file (bytes)", example = "1048576")
    private Long fileSize;
    
    /**
     * Kiểu nội dung file
     */
    @Column(name = "content_type")
    @Schema(description = "MIME type của file", example = "image/jpeg")
    private String contentType;
    
    /**
     * Phần mở rộng file
     */
    @Column(name = "file_extension")
    private String fileExtension;
    
    /**
     * Bucket lưu trữ file
     */
    @Column(name = "storage_bucket")
    @Schema(description = "Tên bucket lưu trữ file", example = "media-bucket")
    private String storageBucket;
    
    /**
     * URL công khai file
     */
    @Column(name = "public_url")
    @Schema(description = "URL công khai để truy cập file", example = "https://media.example.com/images/2023/04/550e8400-e29b-41d4-a716-446655440000.jpg")
    private String publicUrl;
    
    /**
     * ID của user
     */
    @Column(name = "user_id")
    @Schema(description = "ID của người dùng đã tạo media", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    /**
     * Công khai file
     */
    @Column(name = "is_public")
    @Schema(description = "Cho phép truy cập công khai hay không", example = "false")
    private Boolean isPublic;
    
    /**
     * Kiểu media
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    @Schema(description = "Loại media", example = "IMAGE")
    private MediaFileType mediaFileType;
    
    /**
     * Xóa file
     */
    @Column(name = "is_deleted")
    @Schema(description = "Cho biết media đã bị xóa hay chưa", example = "false")
    private Boolean isDeleted;
    
    /**
     * Meta data
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Schema(description = "Thông tin metadata bổ sung dạng JSON")
    private Map<String, String> metaData = new HashMap<>();
    
    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
    
    /**
     * Thời gian xóa
     */
    @Column(name = "deleted_at")
    @Schema(description = "Thời gian xóa")
    private LocalDateTime deletedAt;
    
    /**
     * Chiều rộng
     */
    @Column(name = "width")
    @Schema(description = "Chiều rộng (px) - cho ảnh và video", example = "1920")
    private Integer width;
    
    /**
     * Chiều cao
     */
    @Column(name = "height")
    @Schema(description = "Chiều cao (px) - cho ảnh và video", example = "1080")
    private Integer height;
    
    /**
     * Thời gian duration
     */ 
    @Column(name = "duration")
    @Schema(description = "Thời lượng (ms) - cho audio và video", example = "120000")
    private Long duration;
    
    /**
     * URL thumbnail
     */
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    /**
     * Trạng thái xử lý
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    @Schema(description = "Trạng thái xử lý của file", example = "COMPLETED")
    private ProcessingStatus processingStatus;
    
    /**
     * Lỗi xử lý
     */
    @Column(name = "processing_error")
    private String processingError;
    
    /**
     * Pre persist
     */
    @PrePersist
    public void prePersist() {
        if (isPublic == null) {
            isPublic = false;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    /**
     * Kiểm tra xem media có phải là ảnh không
     */
    public boolean isImage() {
        return MediaFileType.IMAGE.equals(this.mediaFileType);
    }

    /**
     * Kiểm tra xem media có phải là video không
     */
    public boolean isVideo() {
        return MediaFileType.VIDEO.equals(this.mediaFileType);
    }

    /**
     * Kiểm tra xem media có thể xử lý không
     */
    public boolean isProcessable() {
        return isImage() || isVideo();
    }

    /**
     * Kiểm tra xem media đã được xử lý chưa
     */
    public boolean isProcessed() {
        return ProcessingStatus.COMPLETED.equals(this.processingStatus);
    }
}