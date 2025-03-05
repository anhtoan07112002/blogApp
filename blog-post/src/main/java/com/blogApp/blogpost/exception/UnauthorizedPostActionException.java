package com.blogApp.blogpost.exception;

import com.blogApp.blogcommon.exception.UnauthorizedException;

/**
 * Exception khi người dùng không có quyền thực hiện hành động với bài viết
 */
public class UnauthorizedPostActionException extends UnauthorizedException {

    public UnauthorizedPostActionException() {
        super("You don't have permission to perform this action on the post");
    }

    public UnauthorizedPostActionException(String message) {
        super(message);
    }
}
