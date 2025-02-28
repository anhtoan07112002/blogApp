package com.blogApp.blogcommon.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BlogException {

    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }
}
