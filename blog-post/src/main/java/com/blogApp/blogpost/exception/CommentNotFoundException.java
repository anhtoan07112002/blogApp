package com.blogApp.blogpost.exception;

import com.blogApp.blogcommon.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Exception khi không tìm thấy bình luận
 */
public class CommentNotFoundException extends ResourceNotFoundException {

    public CommentNotFoundException(UUID id) {
        super("Comment", "id", id);
    }
}