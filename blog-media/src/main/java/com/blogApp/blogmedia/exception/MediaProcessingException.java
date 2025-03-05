package com.blogApp.blogmedia.exception;

import com.blogApp.blogcommon.constant.ErrorCodes;
import com.blogApp.blogcommon.exception.BlogException;
import org.springframework.http.HttpStatus;

/**
 * Exception được ném ra khi có lỗi trong quá trình xử lý media
 */
public class MediaProcessingException extends BlogException {
    
    public MediaProcessingException(String message) {
        super(message, ErrorCodes.MEDIA_PROCESSING_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    public MediaProcessingException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    public MediaProcessingException(String message, Throwable cause) {
        super(message, ErrorCodes.MEDIA_PROCESSING_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
} 