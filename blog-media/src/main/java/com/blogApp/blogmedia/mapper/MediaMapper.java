package com.blogApp.blogmedia.mapper;

import com.blogApp.blogmedia.dto.MediaDTO;
import com.blogApp.blogmedia.dto.MediaResponseDTO;
import com.blogApp.blogmedia.model.Media;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapper dùng để chuyển đổi Media entity thành DTO và ngược lại
 */
@Component
public class MediaMapper {
    
    private final ObjectMapper objectMapper;
    
    public MediaMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Chuyển đổi Media entity thành MediaDTO
     * @param media Media entity cần chuyển đổi
     * @return MediaDTO tương ứng
     */ 
    public MediaDTO toDTO(Media media) {
        if (media == null) {
            return null;
        }
        
        return MediaDTO.builder()
                .id(media.getId().toString())
                .fileName(media.getFileName())
                .contentType(media.getContentType())
                .size(media.getFileSize())
                .type(media.getMediaFileType())
                .url(media.getPublicUrl())
                .metadata(media.getMetaData())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build();
    }
    
    /**
     * Chuyển đổi Media entity thành MediaResponseDTO
     * @param media Media entity cần chuyển đổi
     * @return MediaResponseDTO tương ứng
     */
    public MediaResponseDTO toResponseDTO(Media media) {
        if (media == null) {
            return null;
        }
        
        return MediaResponseDTO.builder()
                .id(media.getId())
                .fileName(media.getFileName())
                .originalFileName(media.getOriginalFileName())
                .filePath(media.getFilePath())
                .fileSize(media.getFileSize())
                .contentType(media.getContentType())
                .fileExtension(media.getFileExtension())
                .storageBucket(media.getStorageBucket())
                .publicUrl(media.getPublicUrl())
                .mediaFileType(media.getMediaFileType())
                .isPublic(media.getIsPublic())
                .isDeleted(media.getIsDeleted())
                .processingStatus(media.getProcessingStatus())
                .processingError(media.getProcessingError())
                .width(media.getWidth())
                .height(media.getHeight())
                .duration(media.getDuration())
                .thumbnailUrl(media.getThumbnailUrl())
                .metadata(media.getMetaData())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .deletedAt(media.getDeletedAt())
                .build();
    }
    
    /**
     * Chuyển đổi chuỗi JSON thành Map
     * @param jsonString Chuỗi JSON cần chuyển đổi
     * @return Map chứa các cặp key-value   
     */ 
    private Map<String, String> convertStringToMap(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return null;
        }
    }
} 