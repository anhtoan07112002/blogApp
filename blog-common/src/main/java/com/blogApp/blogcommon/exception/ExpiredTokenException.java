package com.blogApp.blogcommon.exception;
import com.blogApp.blogcommon.constant.ErrorCodes;
import org.springframework.http.HttpStatus;

public class ExpiredTokenException extends BlogException {
    public ExpiredTokenException(String message) {
        super(message, ErrorCodes.EXPIRED_TOKEN, HttpStatus.UNAUTHORIZED);
    }
}
