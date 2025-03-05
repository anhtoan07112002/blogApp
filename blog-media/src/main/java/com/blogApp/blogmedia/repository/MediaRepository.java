package com.blogApp.blogmedia.repository;

import com.blogApp.blogmedia.model.Media;
import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogcommon.enums.ProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho Media entity
 * Cung cấp các phương thức query cơ bản và tùy chỉnh
 * Hỗ trợ phân trang và soft delete
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {
    
    // ========== Find by ID ==========
    
    /**
     * Tìm media theo id và userId, chưa bị xóa
     */
    Optional<Media> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);
    
    /**
     * Tìm media theo id, chưa bị xóa
     */
    Optional<Media> findByIdAndIsDeletedFalse(UUID id);
    
    // ========== Find by User ==========
    
    /**
     * Tìm tất cả media của user với phân trang, sắp xếp theo thời gian tạo giảm dần
     */
    Page<Media> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Tìm media theo userId và mediaType với phân trang
     */
    Page<Media> findByUserIdAndMediaTypeAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID userId, MediaFileType mediaFileType, Pageable pageable);
    
    /**
     * Tìm 10 media gần nhất của user
     */
    List<Media> findTop10ByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);
    
    // ========== Find Public Media ==========
    
    /**
     * Tìm tất cả media công khai với phân trang
     */
    Page<Media> findByIsPublicTrueAndIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Tìm media công khai theo mediaType với phân trang
     */
    Page<Media> findByIsPublicTrueAndMediaTypeAndIsDeletedFalseOrderByCreatedAtDesc(
            MediaFileType mediaFileType, Pageable pageable);
    
    // ========== Find by File Info ==========
    
    /**
     * Tìm media theo fileName và chưa bị xóa
     */
    Optional<Media> findByFileNameAndIsDeletedFalse(String fileName);
    
    /**
     * Tìm media theo filePath và chưa bị xóa
     */
    Optional<Media> findByFilePathAndIsDeletedFalse(String filePath);
    
    // ========== Find by Time Range ==========
    
    /**
     * Tìm media theo khoảng thời gian tạo
     */
    List<Media> findByCreatedAtBetweenAndIsDeletedFalse(LocalDateTime start, LocalDateTime end);
    
    /**
     * Tìm media theo userId và khoảng thời gian
     */
    List<Media> findByUserIdAndCreatedAtBetweenAndIsDeletedFalse(UUID userId, LocalDateTime start, LocalDateTime end);
    
    // ========== Find by Processing Status ==========
    
    /**
     * Tìm media theo trạng thái xử lý
     */
    List<Media> findByProcessingStatusAndIsDeletedFalse(ProcessingStatus processingStatus);
    
    /**
     * Tìm media theo userId và trạng thái xử lý
     */
    List<Media> findByUserIdAndProcessingStatusAndIsDeletedFalse(UUID userId, ProcessingStatus processingStatus);
    
    /**
     * Tìm media theo trạng thái xử lý với phân trang
     */
    Page<Media> findByProcessingStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            ProcessingStatus processingStatus, Pageable pageable);
    
    // ========== Find by Media Properties ==========
    
    /**
     * Tìm media theo kích thước
     */
    List<Media> findByWidthAndHeightAndIsDeletedFalse(Integer width, Integer height);
    
    /**
     * Tìm media theo duration
     */
    List<Media> findByDurationAndIsDeletedFalse(Long duration);
    
    /**
     * Tìm media theo khoảng duration
     */
    List<Media> findByDurationBetweenAndIsDeletedFalse(Long minDuration, Long maxDuration);
    
    // ========== Search ==========
    
    /**
     * Tìm media theo từ khóa tìm kiếm
     */
    @Query("SELECT m FROM Media m WHERE m.userId = :userId AND m.isDeleted = false " +
           "AND (LOWER(m.originalFileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(m.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY m.createdAt DESC")
    Page<Media> findBySearchTerm(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Tìm media theo metadata
     */
    @Query("SELECT m FROM Media m WHERE m.userId = :userId AND m.isDeleted = false " +
           "AND m.metaData LIKE %:key% AND m.metaData LIKE %:value% " +
           "ORDER BY m.createdAt DESC")
    Page<Media> findByMetadata(@Param("userId") UUID userId, 
                              @Param("key") String key, 
                              @Param("value") String value, 
                              Pageable pageable);
    
    // ========== Count ==========
    
    /**
     * Đếm số lượng media của user
     */
    @Query("SELECT COUNT(m) FROM Media m WHERE m.userId = :userId AND m.isDeleted = false")
    long countByUserIdAndIsDeletedFalse(@Param("userId") UUID userId);
    
    /**
     * Đếm số lượng media theo loại của user
     */
    long countByUserIdAndMediaTypeAndIsDeletedFalse(UUID userId, MediaFileType mediaFileType);
    
    /**
     * Đếm số lượng media theo trạng thái xử lý
     */
    long countByProcessingStatusAndIsDeletedFalse(ProcessingStatus processingStatus);
    
    // ========== Soft Delete ==========
    
    /**
     * Soft delete media theo id và userId
     */
    @Modifying
    @Query("UPDATE Media m SET m.isDeleted = true, m.deletedAt = :now WHERE m.id = :id AND m.userId = :userId")
    int softDeleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId, @Param("now") LocalDateTime now);
} 