package com.blogApp.blogcommon.exception;

import com.blogApp.blogcommon.constant.ErrorCodes;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BlogException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public BlogException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public BlogException(String message) {
        super(message);
        this.errorCode = ErrorCodes.INTERNAL_SERVER_ERROR;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
