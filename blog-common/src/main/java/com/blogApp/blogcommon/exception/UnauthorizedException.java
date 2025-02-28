package com.blogApp.blogcommon.exception;

import com.blogApp.blogcommon.constant.ErrorCodes;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BlogException {

    public UnauthorizedException(String message) {
        super(message, ErrorCodes.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }
}
