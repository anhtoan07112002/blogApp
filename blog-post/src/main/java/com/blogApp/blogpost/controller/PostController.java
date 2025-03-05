package com.blogApp.blogpost.controller;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.enums.PostStatus;
import com.blogApp.blogpost.dto.request.PostCreateRequest;
import com.blogApp.blogpost.dto.request.PostUpdateRequest;
import com.blogApp.blogpost.dto.response.PostSummaryDTO;
import com.blogApp.blogpost.service.interfaces.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller xử lý các yêu cầu liên quan đến bài viết
 * - Cung cấp các API CRUD cho bài viết
 * - Hỗ trợ phân trang và sắp xếp
 * - Tích hợp với caching để tối ưu hiệu suất
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Tạo bài viết mới
     * @param request DTO chứa thông tin bài viết
     * @param userId ID của người dùng tạo bài viết
     * @return PostSummaryDTO chứa thông tin bài viết đã tạo
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostSummaryDTO> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(postService.createPost(request, userId));
    }

    /**
     * Lấy bài viết theo ID
     * @param id ID của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostSummaryDTO> getPostById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    /**
     * Lấy bài viết theo slug
     * @param slug slug của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostSummaryDTO> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }

    /**
     * Cập nhật thông tin bài viết
     * @param id ID của bài viết cần cập nhật
     * @param request DTO chứa thông tin cập nhật
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostSummaryDTO> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    /**
     * Xóa bài viết
     * @param id ID của bài viết cần xóa
     * @return ResponseEntity không có nội dung
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy tất cả bài viết
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Tên trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    @GetMapping
    public ResponseEntity<PagedResponse<PostSummaryDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(postService.getAllPosts(pageNo, pageSize, sortBy, sortDir));
    }

    /**
     * Lấy danh sách bài viết theo tác giả
     * @param authorId ID của tác giả
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Tên trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<PagedResponse<PostSummaryDTO>> getPostsByAuthor(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId, pageNo, pageSize, sortBy, sortDir));
    }

    /**
     * Lấy danh sách bài viết theo tag
     * @param tagId ID của tag
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Tên trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<PagedResponse<PostSummaryDTO>> getPostsByTag(
            @PathVariable UUID tagId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(postService.getPostsByTag(tagId, pageNo, pageSize, sortBy, sortDir));
    }

    /**
     * Tìm kiếm bài viết
     * @param keyword Từ khóa tìm kiếm
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<PostSummaryDTO>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(postService.searchPosts(keyword, pageNo, pageSize));
    }

    /**
     * Cập nhật trạng thái bài viết
     * @param id ID của bài viết
     * @param status Trạng thái mới
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostSummaryDTO> updatePostStatus(
            @PathVariable UUID id,
            @RequestParam PostStatus status) {
        return ResponseEntity.ok(postService.updatePostStatus(id, status));
    }

    /**
     * Tăng số lượt xem bài viết
     * @param id ID của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<PostSummaryDTO> incrementViewCount(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.incrementViewCount(id));
    }
} 