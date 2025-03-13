package com.blogApp.blogmedia.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity đại diện cho mối quan hệ nhiều-nhiều giữa Media và Post
 * Lưu trữ thông tin về việc Media nào được sử dụng trong Post nào
 */
@Entity
@Table(name = "media_post")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin liên kết giữa media và bài viết")
public class MediaPost {
    
    /**
     * ID duy nhất của bản ghi
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "ID duy nhất của liên kết", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    /**
     * ID của media
     */
    @Column(name = "media_id", nullable = false)
    @Schema(description = "ID của media", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID mediaId;
    
    /**
     * ID của post
     */
    @Column(name = "post_id", nullable = false)
    @Schema(description = "ID của bài viết", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID postId;
    
    /**
     * Loại media trong post (avatar, cover, content, etc.)
     */
    @Column(name = "type")
    @Schema(description = "Loại media trong bài viết", example = "FEATURED_IMAGE", allowableValues = {"FEATURED_IMAGE", "CONTENT_IMAGE", "GALLERY", "ATTACHMENT"})
    private String type;
    
    /**
     * Vị trí media trong post (thứ tự hiển thị)
     */
    @Column(name = "position")
    @Schema(description = "Vị trí hiển thị media trong bài viết", example = "1")
    private Integer position;
    
    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Thời gian tạo liên kết")
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
    
    /**
     * Metadata bổ sung
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Schema(description = "Metadata bổ sung cho liên kết")
    private String metadata;
} 