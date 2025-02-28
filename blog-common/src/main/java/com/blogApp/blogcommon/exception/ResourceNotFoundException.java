package com.blogApp.blogcommon.exception;

import com.blogApp.blogcommon.constant.ErrorCodes;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BlogException {

    public ResourceNotFoundException(String message) {
        super(message, ErrorCodes.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue),
                ErrorCodes.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}