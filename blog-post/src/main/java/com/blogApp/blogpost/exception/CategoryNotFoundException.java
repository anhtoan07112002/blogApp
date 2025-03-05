package com.blogApp.blogpost.exception;

import com.blogApp.blogcommon.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Exception khi không tìm thấy danh mục
 */
public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException(UUID id) {
        super("Category", "id", id);
    }

    public CategoryNotFoundException(String slug) {
        super("Category", "slug", slug);
    }
}
