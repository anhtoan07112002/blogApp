package com.blogApp.blogpost.exception;

import com.blogApp.blogcommon.exception.UnauthorizedException;

/**
 * Exception dành riêng cho các hành động không được phép trên bình luận
 */
public class UnauthorizedCommentActionException extends UnauthorizedException {

    public UnauthorizedCommentActionException(String message) {
        super(message);
    }
    
    public static UnauthorizedCommentActionException notAuthor() {
        return new UnauthorizedCommentActionException("Bạn không phải là tác giả của bình luận này");
    }
    
    public static UnauthorizedCommentActionException notAuthorOrAdmin() {
        return new UnauthorizedCommentActionException("Bạn không có quyền chỉnh sửa hoặc xóa bình luận này vì không phải tác giả hoặc admin");
    }
    
    public static UnauthorizedCommentActionException cannotUpdateComment() {
        return new UnauthorizedCommentActionException("Bạn không có quyền chỉnh sửa bình luận này vì không phải tác giả hoặc admin");
    }
    
    public static UnauthorizedCommentActionException cannotDeleteComment() {
        return new UnauthorizedCommentActionException("Bạn không có quyền xóa bình luận này vì không phải tác giả hoặc admin");
    }
} 