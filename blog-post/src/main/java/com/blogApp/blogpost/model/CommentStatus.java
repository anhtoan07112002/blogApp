package com.blogApp.blogpost.model;

/**
 * Enum định nghĩa các trạng thái của bình luận
 * - PENDING: Chờ duyệt
 * - APPROVED: Đã duyệt
 * - SPAM: Spam 
 * - DELETED: Đã xóa
 */
public enum CommentStatus {
    PENDING,
    APPROVED,
    SPAM,
    DELETED
}
