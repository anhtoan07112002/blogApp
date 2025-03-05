package com.blogApp.blogmedia.model;

import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogcommon.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {
    /**
     * ID của media
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Tên file
     */
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    /**
     * Tên file gốc
     */
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;
    
    /**
     * Đường dẫn file
     */
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    /**
     * Kích thước file
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    /**
     * Kiểu nội dung file
     */
    @Column(name = "content_type", nullable = false)
    private String contentType;
    
    /**
     * Phần mở rộng file
     */
    @Column(name = "file_extension")
    private String fileExtension;
    
    /**
     * Bucket lưu trữ file
     */
    @Column(name = "storage_bucket", nullable = false)
    private String storageBucket;
    
    /**
     * URL công khai file
     */
    @Column(name = "public_url")
    private String publicUrl;
    
    /**
     * ID của user
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    /**
     * Công khai file
     */
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;
    
    /**
     * Kiểu media
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaFileType mediaFileType;
    
    /**
     * Xóa file
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
    
    /**
     * Meta data
     */
    @Type(com.vladmihalcea.hibernate.type.json.JsonType.class)
    @Column(name = "meta_data", columnDefinition = "jsonb")
    private Map<String, String> metaData;
    
    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Thời gian xóa
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * Chiều rộng
     */
    @Column(name = "width")
    private Integer width;
    
    /**
     * Chiều cao
     */
    @Column(name = "height")
    private Integer height;
    
    /**
     * Thời gian duration
     */ 
    @Column(name = "duration")
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