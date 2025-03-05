package com.blogApp.blogmedia.exception;

import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import java.util.UUID;

/**
 * Exception được ném ra khi không tìm thấy media
 */
public class MediaNotFoundException extends ResourceNotFoundException {
    
    public MediaNotFoundException(UUID id) {
        super(String.format("Không tìm thấy media với id: %s", id));
    }
    
    public MediaNotFoundException(String fileName) {
        super(String.format("Không tìm thấy media với tên file: %s", fileName));
    }
    
    public MediaNotFoundException(UUID id, UUID userId) {
        super(String.format("Không tìm thấy media với id: %s của user: %s", id, userId));
    }
} 