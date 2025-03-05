package com.blogApp.blogmedia.exception;

import com.blogApp.blogcommon.exception.UnauthorizedException;
import java.util.UUID;

/**
 * Exception được ném ra khi user không có quyền thực hiện hành động với media
 */
public class UnauthorizedMediaActionException extends UnauthorizedException {
    
    public UnauthorizedMediaActionException(UUID mediaId, UUID userId) {
        super(String.format("User %s không có quyền thực hiện hành động với media %s", userId, mediaId));
    }
    
    public UnauthorizedMediaActionException(String message) {
        super(message);
    }
} 