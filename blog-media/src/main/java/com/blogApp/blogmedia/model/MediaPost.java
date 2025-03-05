package com.blogApp.blogmedia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity quản lý quan hệ nhiều-nhiều giữa Media và Post
 * - Lưu thông tin về vị trí media trong post
 * - Lưu thông tin về loại media trong post
 * - Tự động quản lý thời gian tạo/cập nhật
 */
@Entity
@Table(name = "media_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaPost {
    
    /**
     * ID của media trong post
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * ID của media
     */
    @Column(name = "media_id", nullable = false)
    private UUID mediaId;
    
    /**
     * ID của post
     */
    @Column(name = "post_id", nullable = false)
    private UUID postId;
    
    /**
     * Vị trí media trong post
     */
    @Column(name = "position")
    private Integer position;
    
    /**
     * Loại media trong post (thumbnail, content, etc.)
     */
    @Column(name = "type")
    private String type;
    
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
} 