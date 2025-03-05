package com.blogApp.blogmedia.dto;

import com.blogApp.blogcommon.enums.MediaFileType;
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
public class MediaDTO {
    private String id;
    private String fileName;
    private String contentType;
    private Long size;
    private MediaFileType type;
    private String url;
    private Map<String, String> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
