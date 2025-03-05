package com.blogApp.blogpost.exception;

import com.blogApp.blogcommon.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Exception khi không tìm thấy bài viết
 */
public class PostNotFoundException extends ResourceNotFoundException {

    public PostNotFoundException(UUID id) {
        super("Post", "id", id);
    }

    public PostNotFoundException(String slug) {
        super("Post", "slug", slug);
    }
}

