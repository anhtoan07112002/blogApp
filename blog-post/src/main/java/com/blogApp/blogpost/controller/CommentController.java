package com.blogApp.blogpost.controller;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogpost.dto.request.CommentCreateRequest;
import com.blogApp.blogpost.dto.response.CommentDTO;
import com.blogApp.blogpost.model.CommentStatus;
import com.blogApp.blogpost.service.interfaces.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller xử lý các yêu cầu liên quan đến bình luận
 * - Cung cấp các API CRUD cho bình luận
 * - Hỗ trợ phân trang và sắp xếp
 * - Tích hợp với caching để tối ưu hiệu suất
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Tạo bình luận mới
     * @param postId ID của bài viết
     * @param request DTO chứa thông tin bình luận
     * @param userId ID của người dùng tạo bình luận
     * @return CommentDTO chứa thông tin bình luận đã tạo
     */
    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(commentService.createComment(postId, request, userId));
    }

    /**
     * Lấy bình luận theo ID
     * @param id ID của bình luận
     * @return CommentDTO chứa thông tin bình luận
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable UUID id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    /**
     * Cập nhật nội dung bình luận
     * @param id ID của bình luận cần cập nhật
     * @param content Nội dung mới
     * @return CommentDTO chứa thông tin bình luận đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody String content) {
        return ResponseEntity.ok(commentService.updateComment(id, content));
    }

    /**
     * Xóa bình luận
     * @param id ID của bình luận cần xóa
     * @return ResponseEntity không có nội dung
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy danh sách bình luận theo bài viết
     * @param postId ID của bài viết
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<CommentDTO> chứa danh sách bình luận
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<PagedResponse<CommentDTO>> getCommentsByPost(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId, pageNo, pageSize));
    }

    /**
     * Lấy danh sách bình luận theo tác giả
     * @param authorId ID của tác giả
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<CommentDTO> chứa danh sách bình luận
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<PagedResponse<CommentDTO>> getCommentsByAuthor(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(commentService.getCommentsByAuthor(authorId, pageNo, pageSize));
    }

    /**
     * Lấy danh sách bình luận theo trạng thái
     * @param status Trạng thái bình luận
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<CommentDTO> chứa danh sách bình luận
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<PagedResponse<CommentDTO>> getCommentsByStatus(
            @PathVariable CommentStatus status,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(commentService.getCommentsByStatus(status, pageNo, pageSize));
    }

    /**
     * Cập nhật trạng thái bình luận
     * @param id ID của bình luận
     * @param status Trạng thái mới
     * @return CommentDTO chứa thông tin bình luận đã cập nhật
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommentDTO> updateCommentStatus(
            @PathVariable UUID id,
            @RequestParam CommentStatus status) {
        return ResponseEntity.ok(commentService.updateCommentStatus(id, status));
    }
} 