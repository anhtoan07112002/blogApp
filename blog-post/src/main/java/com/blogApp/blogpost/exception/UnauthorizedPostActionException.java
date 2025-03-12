package com.blogApp.blogpost.exception;

import com.blogApp.blogcommon.exception.UnauthorizedException;

/**
 * Exception dành riêng cho các hành động không được phép trên bài viết
 */
public class UnauthorizedPostActionException extends UnauthorizedException {

    public UnauthorizedPostActionException(String message) {
        super(message);
    }
    
    public static UnauthorizedPostActionException notAuthor() {
        return new UnauthorizedPostActionException("Bạn không phải là tác giả của bài viết này");
    }
    
    public static UnauthorizedPostActionException notAuthorOrAdmin() {
        return new UnauthorizedPostActionException("Bạn không có quyền xóa bài viết này vì không phải tác giả hoặc admin");
    }
    
    public static UnauthorizedPostActionException cannotUpdateStatus() {
        return new UnauthorizedPostActionException("Bạn không có quyền thay đổi trạng thái bài viết này vì không phải tác giả hoặc admin");
    }
}
