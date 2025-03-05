package com.blogApp.blogmedia.service.impl;

import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogmedia.client.AuthServiceClient;
import com.blogApp.blogmedia.dto.MediaDTO;
import com.blogApp.blogmedia.exception.MediaProcessingException;
import com.blogApp.blogmedia.exception.MediaUploadException;
import com.blogApp.blogmedia.model.Media;
import com.blogApp.blogmedia.model.MediaPost;
import com.blogApp.blogmedia.repository.MediaPostRepository;
import com.blogApp.blogmedia.repository.MediaRepository;
import com.blogApp.blogmedia.service.interfaces.MediaService;
import com.blogApp.blogmedia.service.interfaces.MinioService;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation của MediaService
 * Xử lý logic nghiệp vụ liên quan đến media
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MinioService minioService;
    private final MediaRepository mediaRepository;
    private final MediaPostRepository mediaPostRepository;
    private final CacheService cacheService;
    private final AuthServiceClient authServiceClient;
    
    private static final String CACHE_TYPE = AppConstants.MEDIA_CACHE;
    private static final long CACHE_TTL = AppConstants.MEDIA_CACHE_TTL;

    @Override
    @Transactional
    public MediaDTO uploadMedia(MultipartFile file, MediaFileType type) {
        return uploadMediaWithMetadata(file, type, new HashMap<>());
    }

    @Override
    @Transactional
    public MediaDTO uploadMediaWithMetadata(MultipartFile file, MediaFileType type, Map<String, String> metadata) {
        // Validate file
        if (!validateMediaSize(file.getSize())) {
            throw new MediaUploadException("File quá lớn. Kích thước tối đa là " + AppConstants.MAX_FILE_SIZE / (1024 * 1024) + "MB");
        }
        
        // Validate extension
        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        if (!type.isAllowedExtension(extension)) {
            throw new MediaUploadException("Loại file không được hỗ trợ");
        }
        
        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String objectName = UUID.randomUUID().toString() + extension;
        
        // Thêm metadata
        Map<String, String> fullMetadata = new HashMap<>(metadata);
        fullMetadata.put("mediaType", type.getType());
        fullMetadata.put("originalFilename", originalFilename);
        
        try {
            // Upload file
            String url = minioService.uploadFile(file, objectName, file.getContentType(), fullMetadata);
            
            // Tạo Media entity
            Media media = Media.builder()
                    .id(UUID.randomUUID())
                    .fileName(objectName)
                    .originalFileName(originalFilename)
                    .filePath(objectName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .fileExtension(extension)
                    .storageBucket(minioService.getBucketName())
                    .publicUrl(url)
                    .mediaFileType(type)
                    .isPublic(false)
                    .isDeleted(false)
                    .metaData(fullMetadata)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // Lưu vào database
            media = mediaRepository.save(media);
            
            // Tạo MediaDTO
            MediaDTO mediaDTO = MediaDTO.builder()
                    .id(media.getId().toString())
                    .fileName(originalFilename)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .type(type)
                    .url(url)
                    .metadata(fullMetadata)
                    .createdAt(media.getCreatedAt())
                    .updatedAt(media.getUpdatedAt())
                    .build();
            
            // Cache kết quả
            cacheService.set(CACHE_TYPE, media.getId().toString(), mediaDTO, CACHE_TTL, java.util.concurrent.TimeUnit.SECONDS);
            
            return mediaDTO;
        } catch (Exception e) {
            log.error("Lỗi upload media: {}", e.getMessage(), e);
            throw new MediaUploadException("Không thể upload media", e);
        }
    }

    @Override
    public MediaDTO getMedia(String mediaId) {
        // Thử lấy từ cache trước
        MediaDTO cachedMedia = (MediaDTO) cacheService.get(CACHE_TYPE, mediaId);
        if (cachedMedia != null) {
            return cachedMedia;
        }
        
        try {
            // Lấy từ database
            Media media = mediaRepository.findByIdAndIsDeletedFalse(UUID.fromString(mediaId))
                    .orElseThrow(() -> new MediaProcessingException("Media không tồn tại"));
            
            // Lấy thông tin từ MinIO
            StatObjectResponse objectInfo = minioService.getObjectInfo(media.getFileName());
            Map<String, String> metadata = objectInfo.userMetadata();
            
            // Tạo MediaDTO
            MediaDTO mediaDTO = MediaDTO.builder()
                    .id(media.getId().toString())
                    .fileName(metadata.get("originalFilename"))
                    .contentType(objectInfo.contentType())
                    .size(objectInfo.size())
                    .type(media.getMediaFileType())
                    .url(media.getPublicUrl())
                    .metadata(metadata)
                    .createdAt(media.getCreatedAt())
                    .updatedAt(media.getUpdatedAt())
                    .build();
            
            // Cache kết quả
            cacheService.set(CACHE_TYPE, mediaId, mediaDTO, CACHE_TTL, java.util.concurrent.TimeUnit.SECONDS);
            
            return mediaDTO;
        } catch (Exception e) {
            log.error("Lỗi lấy thông tin media: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể lấy thông tin media", e);
        }
    }

    @Override
    public InputStream getMediaContent(String mediaId) {
        try {
            // Lấy từ database để kiểm tra tồn tại
            Media media = mediaRepository.findByIdAndIsDeletedFalse(UUID.fromString(mediaId))
                    .orElseThrow(() -> new MediaProcessingException("Media không tồn tại"));
            
            return minioService.getFile(media.getFileName());
        } catch (Exception e) {
            log.error("Lỗi lấy nội dung media: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể lấy nội dung media", e);
        }
    }

    @Override
    public String getMediaUrl(String mediaId) {
        // Thử lấy từ cache trước
        MediaDTO cachedMedia = (MediaDTO) cacheService.get(CACHE_TYPE, mediaId);
        if (cachedMedia != null) {
            return cachedMedia.getUrl();
        }
        
        // Lấy từ database
        Media media = mediaRepository.findByIdAndIsDeletedFalse(UUID.fromString(mediaId))
                .orElseThrow(() -> new MediaProcessingException("Media không tồn tại"));
        
        return media.getPublicUrl();
    }

    @Override
    @Transactional
    public void deleteMedia(String mediaId) {
        try {
            // Soft delete trong database
            Media media = mediaRepository.findByIdAndIsDeletedFalse(UUID.fromString(mediaId))
                    .orElseThrow(() -> new MediaProcessingException("Media không tồn tại"));
            
            media.setIsDeleted(true);
            media.setDeletedAt(LocalDateTime.now());
            mediaRepository.save(media);
            
            // Xóa khỏi MinIO
            minioService.deleteFile(media.getFileName());
            
            // Xóa khỏi cache
            cacheService.delete(CACHE_TYPE, mediaId);
            
            // Xóa các quan hệ với post
            mediaPostRepository.deleteByMediaId(media.getId());
        } catch (Exception e) {
            log.error("Lỗi xóa media: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể xóa media", e);
        }
    }

    @Override
    @Transactional
    public MediaDTO updateMediaMetadata(String mediaId, Map<String, String> metadata) {
        try {
            // Lấy thông tin media hiện tại
            Media media = mediaRepository.findByIdAndIsDeletedFalse(UUID.fromString(mediaId))
                    .orElseThrow(() -> new MediaProcessingException("Media không tồn tại"));
            
            // Cập nhật metadata
            Map<String, String> updatedMetadata = new HashMap<>(media.getMetaData());
            updatedMetadata.putAll(metadata);
            
            // Tạo object mới với metadata mới
            String newObjectName = UUID.randomUUID().toString() + "." + 
                    media.getOriginalFileName().substring(media.getOriginalFileName().lastIndexOf(".") + 1);
            
            // Copy file với metadata mới
            String url = minioService.uploadFile(
                    minioService.getFile(media.getFileName()),
                    newObjectName,
                    media.getFileSize(),
                    media.getContentType(),
                    updatedMetadata
            );
            
            // Xóa file cũ
            minioService.deleteFile(media.getFileName());
            
            // Cập nhật trong database
            media.setFileName(newObjectName);
            media.setFilePath(newObjectName);
            media.setPublicUrl(url);
            media.setMetaData(updatedMetadata);
            media.setUpdatedAt(LocalDateTime.now());
            media = mediaRepository.save(media);
            
            // Tạo MediaDTO mới
            MediaDTO mediaDTO = MediaDTO.builder()
                    .id(media.getId().toString())
                    .fileName(media.getOriginalFileName())
                    .contentType(media.getContentType())
                    .size(media.getFileSize())
                    .type(media.getMediaFileType())
                    .url(url)
                    .metadata(updatedMetadata)
                    .createdAt(media.getCreatedAt())
                    .updatedAt(media.getUpdatedAt())
                    .build();
            
            // Cập nhật cache
            cacheService.set(CACHE_TYPE, mediaId, mediaDTO, CACHE_TTL, java.util.concurrent.TimeUnit.SECONDS);
            
            return mediaDTO;
        } catch (Exception e) {
            log.error("Lỗi cập nhật metadata: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể cập nhật metadata", e);
        }
    }

    @Override
    public boolean validateMediaType(String mediaType) {
        try {
            MediaFileType type = MediaFileType.fromString(mediaType);
            return type != MediaFileType.OTHER;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean validateMediaSize(long size) {
        return size <= AppConstants.MAX_FILE_SIZE;
    }
    
    /**
     * Thêm media vào post
     * @param mediaId ID của media
     * @param postId ID của post
     * @param type Loại media trong post
     * @param position Vị trí media trong post
     */
    @Transactional
    public void addMediaToPost(UUID mediaId, UUID postId, String type, Integer position) {
        try {
            // Kiểm tra media tồn tại
            Media media = mediaRepository.findByIdAndIsDeletedFalse(mediaId)
                    .orElseThrow(() -> new MediaProcessingException("Media không tồn tại"));
            
            // Tạo quan hệ mới
            MediaPost mediaPost = MediaPost.builder()
                    .mediaId(mediaId)
                    .postId(postId)
                    .type(type)
                    .position(position)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            mediaPostRepository.save(mediaPost);
        } catch (Exception e) {
            log.error("Lỗi thêm media vào post: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể thêm media vào post", e);
        }
    }
    
    /**
     * Xóa media khỏi post
     * @param mediaId ID của media
     * @param postId ID của post
     */
    @Transactional
    public void removeMediaFromPost(UUID mediaId, UUID postId) {
        try {
            mediaPostRepository.findByMediaIdAndPostId(mediaId, postId)
                    .ifPresent(mediaPostRepository::delete);
        } catch (Exception e) {
            log.error("Lỗi xóa media khỏi post: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể xóa media khỏi post", e);
        }
    }
    
    /**
     * Lấy danh sách media của post
     * @param postId ID của post
     * @return Danh sách media
     */
    public List<MediaDTO> getPostMedia(UUID postId) {
        try {
            List<MediaPost> mediaPosts = mediaPostRepository.findByPostId(postId);
            return mediaPosts.stream()
                    .map(mediaPost -> getMedia(mediaPost.getMediaId().toString()))
                    .toList();
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách media của post: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể lấy danh sách media của post", e);
        }
    }
}
