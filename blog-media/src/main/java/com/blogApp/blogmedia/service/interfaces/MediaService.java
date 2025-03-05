package com.blogApp.blogmedia.service.interfaces;

import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogmedia.dto.MediaDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * Service interface cho việc xử lý media
 * Cung cấp các phương thức để:
 * - Upload media với validation
 * - Lấy thông tin media
 * - Xóa media
 * - Cập nhật metadata
 * - Validate media
 */
public interface MediaService {
    
    /**
     * Upload media với validation cơ bản
     * 
     * @param file File cần upload
     * @param type Loại media
     * @return MediaDTO chứa thông tin media đã upload
     */
    MediaDTO uploadMedia(MultipartFile file, MediaFileType type);
    
    /**
     * Upload media với metadata
     * 
     * @param file File cần upload
     * @param type Loại media
     * @param metadata Metadata bổ sung
     * @return MediaDTO chứa thông tin media đã upload
     */
    MediaDTO uploadMediaWithMetadata(MultipartFile file, MediaFileType type, Map<String, String> metadata);
    
    /**
     * Lấy thông tin media theo ID
     * 
     * @param mediaId ID của media
     * @return MediaDTO chứa thông tin media
     */
    MediaDTO getMedia(String mediaId);
    
    /**
     * Lấy nội dung media
     * 
     * @param mediaId ID của media
     * @return InputStream chứa nội dung media
     */
    InputStream getMediaContent(String mediaId);
    
    /**
     * Lấy URL công khai của media
     * 
     * @param mediaId ID của media
     * @return URL công khai
     */
    String getMediaUrl(String mediaId);
    
    /**
     * Xóa media
     * 
     * @param mediaId ID của media
     */
    void deleteMedia(String mediaId);
    
    /**
     * Cập nhật metadata của media
     * 
     * @param mediaId ID của media
     * @param metadata Metadata mới
     * @return MediaDTO đã được cập nhật
     */
    MediaDTO updateMediaMetadata(String mediaId, Map<String, String> metadata);
    
    /**
     * Validate loại media
     * 
     * @param mediaType Loại media cần validate
     * @return true nếu hợp lệ, false nếu không
     */
    boolean validateMediaType(String mediaType);
    
    /**
     * Validate kích thước media
     * 
     * @param size Kích thước file (bytes)
     * @return true nếu hợp lệ, false nếu không
     */
    boolean validateMediaSize(long size);
}
