package com.blogApp.blogpost.service.interfaces;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogpost.dto.request.CommentCreateRequest;
import com.blogApp.blogpost.dto.response.CommentDTO;
import com.blogApp.blogpost.model.CommentStatus;

import java.util.UUID;

/**
 * Interface cho CommentService
 * - Tạo bình luận mới
 * - Lấy bình luận theo ID
 * - Cập nhật bình luận
 * - Xóa bình luận
 * - Lấy danh sách bình luận theo bài viết
 * - Lấy danh sách bình luận theo tác giả
 * - Lấy danh sách bình luận theo trạng thái
 * - Cập nhật trạng thái bình luận
 */
public interface CommentService {

    /**
     * Tạo bình luận mới
     * @param postId ID của bài viết
     * @param request Thông tin bình luận
     * @param userId ID của người bình luận
     * @return CommentDTO chứa thông tin bình luận đã tạo
     */
    CommentDTO createComment(UUID postId, CommentCreateRequest request, String userId);

    /**
     * Lấy thông tin bình luận theo ID
     * @param id ID của bình luận
     * @return CommentDTO chứa thông tin bình luận
     */
    CommentDTO getCommentById(UUID id);

    /**
     * Cập nhật nội dung bình luận
     * @param id ID của bình luận
     * @param content Nội dung mới
     * @return CommentDTO chứa thông tin bình luận đã cập nhật
     */
    CommentDTO updateComment(UUID id, String content);

    /**
     * Xóa bình luận
     * @param id ID của bình luận
     */
    void deleteComment(UUID id);

    /**
     * Lấy danh sách bình luận của bài viết
     * @param postId ID của bài viết
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<CommentDTO> chứa danh sách bình luận
     */
    PagedResponse<CommentDTO> getCommentsByPost(UUID postId, int pageNo, int pageSize);

    /**
     * Lấy danh sách bình luận của tác giả
     * @param authorId ID của tác giả
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<CommentDTO> chứa danh sách bình luận
     */
    PagedResponse<CommentDTO> getCommentsByAuthor(String authorId, int pageNo, int pageSize);

    /**
     * Lấy danh sách bình luận theo trạng thái
     * @param status Trạng thái bình luận
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<CommentDTO> chứa danh sách bình luận
     */
    PagedResponse<CommentDTO> getCommentsByStatus(CommentStatus status, int pageNo, int pageSize);

    /**
     * Cập nhật trạng thái bình luận
     * @param id ID của bình luận
     * @param status Trạng thái mới
     * @return CommentDTO chứa thông tin bình luận đã cập nhật
     */
    CommentDTO updateCommentStatus(UUID id, CommentStatus status);
}
