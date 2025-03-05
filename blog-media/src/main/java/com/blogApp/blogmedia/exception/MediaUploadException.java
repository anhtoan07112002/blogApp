package com.blogApp.blogmedia.exception;

import com.blogApp.blogcommon.constant.ErrorCodes;
import com.blogApp.blogcommon.exception.BlogException;
import org.springframework.http.HttpStatus;

/**
 * Exception được ném ra khi có lỗi trong quá trình upload media
 */
public class MediaUploadException extends BlogException {
    
    public MediaUploadException(String message) {
        super(message, ErrorCodes.MEDIA_UPLOAD_ERROR, HttpStatus.BAD_REQUEST);
    }
    
    public MediaUploadException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
    
    public MediaUploadException(String message, Throwable cause) {
        super(message, ErrorCodes.MEDIA_UPLOAD_ERROR, HttpStatus.BAD_REQUEST);
        initCause(cause);
    }
} 