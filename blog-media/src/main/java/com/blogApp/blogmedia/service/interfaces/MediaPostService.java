package com.blogApp.blogmedia.service.interfaces;

import com.blogApp.blogmedia.dto.MediaDTO;
import java.util.List;
import java.util.UUID;

/**
 * Service interface cho việc quản lý media trong post
 */
public interface MediaPostService {
    
    /**
     * Thêm media vào post
     * @param mediaId ID của media
     * @param postId ID của post
     * @param type Loại media trong post
     * @param position Vị trí media trong post
     */
    void addMediaToPost(UUID mediaId, UUID postId, String type, Integer position);
    
    /**
     * Xóa media khỏi post
     * @param mediaId ID của media
     * @param postId ID của post
     */
    void removeMediaFromPost(UUID mediaId, UUID postId);
    
    /**
     * Lấy danh sách media của post
     * @param postId ID của post
     * @return Danh sách media
     */
    List<MediaDTO> getPostMedia(UUID postId);
} 