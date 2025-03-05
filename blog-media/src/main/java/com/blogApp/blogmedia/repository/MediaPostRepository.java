package com.blogApp.blogmedia.repository;

import com.blogApp.blogmedia.model.MediaPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho MediaPost
 * - Quản lý quan hệ nhiều-nhiều giữa Media và Post
 * - Cung cấp các phương thức query cơ bản và tùy chỉnh
 */
@Repository
public interface MediaPostRepository extends JpaRepository<MediaPost, UUID> {
    
    /**
     * Tìm tất cả media của post
     */
    List<MediaPost> findByPostId(UUID postId);
    
    /**
     * Tìm tất cả post của media
     */
    List<MediaPost> findByMediaId(UUID mediaId);
    
    /**
     * Tìm media theo postId và type
     */
    List<MediaPost> findByPostIdAndType(UUID postId, String type);
    
    /**
     * Tìm media theo mediaId và postId
     */
    Optional<MediaPost> findByMediaIdAndPostId(UUID mediaId, UUID postId);
    
    /**
     * Xóa tất cả media của post
     */
    @Modifying
    @Query("DELETE FROM MediaPost mp WHERE mp.postId = :postId")
    void deleteByPostId(@Param("postId") UUID postId);
    
    /**
     * Xóa tất cả post của media
     */
    @Modifying
    @Query("DELETE FROM MediaPost mp WHERE mp.mediaId = :mediaId")
    void deleteByMediaId(@Param("mediaId") UUID mediaId);
} 