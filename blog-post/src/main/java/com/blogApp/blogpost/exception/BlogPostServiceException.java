package com.blogApp.blogpost.exception;

import com.blogApp.blogcommon.exception.BlogException;
import org.springframework.http.HttpStatus;

/**
 * Exception chung cho blog-post service
 */
public class BlogPostServiceException extends BlogException {

    public BlogPostServiceException(String message) {
        super(message, "BLOG_POST_SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public BlogPostServiceException(String message, HttpStatus status) {
        super(message, "BLOG_POST_SERVICE_ERROR", status);
    }

    public BlogPostServiceException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }
}
