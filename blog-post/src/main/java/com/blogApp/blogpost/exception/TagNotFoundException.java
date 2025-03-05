package com.blogApp.blogpost.exception;

import com.blogApp.blogcommon.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Exception khi không tìm thấy tag
 */
public class TagNotFoundException extends ResourceNotFoundException {

    public TagNotFoundException(UUID id) {
        super("Tag", "id", id);
    }

    public TagNotFoundException(String name) {
        super("Tag", "name", name);
    }
}
