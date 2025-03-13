package com.blogApp.blogpost.controller;

import com.blogApp.blogcommon.dto.UserPrincipal;
import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogpost.dto.request.CommentCreateRequest;
import com.blogApp.blogpost.dto.response.CommentDTO;
import com.blogApp.blogpost.model.CommentStatus;
import com.blogApp.blogpost.service.interfaces.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

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
@Tag(name = "Comments", description = "API quản lý bình luận")
public class CommentController {

    private final CommentService commentService;

    /**
     * Tạo bình luận mới
     * @param postId ID của bài viết
     * @param request DTO chứa thông tin bình luận
     * @param userPrincipal Thông tin người dùng đã xác thực
     * @return CommentDTO chứa thông tin bình luận đã tạo
     */
    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Tạo bình luận mới", 
            description = "Tạo một bình luận mới cho bài viết",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tạo bình luận thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = CommentDTO.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Dữ liệu không hợp lệ"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(commentService.createComment(postId, request, userPrincipal.getId().toString()));
    }

    /**
     * Lấy bình luận theo ID
     * @param id ID của bình luận
     * @return CommentDTO chứa thông tin bình luận
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy bình luận theo ID", 
            description = "Trả về thông tin chi tiết của bình luận theo ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy bình luận thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = CommentDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bình luận")
    })
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable UUID id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    /**
     * Cập nhật nội dung bình luận
     * @param id ID của bình luận
     * @param content Nội dung mới của bình luận
     * @param userPrincipal Thông tin người dùng đã xác thực
     * @return CommentDTO chứa thông tin bình luận đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cập nhật bình luận", 
            description = "Cập nhật nội dung của bình luận theo ID",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cập nhật bình luận thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = CommentDTO.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Dữ liệu không hợp lệ"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bình luận")
    })
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody String content,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(commentService.updateComment(id, content, userPrincipal.getId().toString()));
    }

    /**
     * Xóa bình luận
     * @param id ID của bình luận
     * @param userPrincipal Thông tin người dùng đã xác thực
     * @return Không có nội dung
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Xóa bình luận", 
            description = "Xóa bình luận theo ID",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Xóa bình luận thành công"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bình luận")
    })
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {
        commentService.deleteComment(id, userPrincipal.getId().toString());
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy danh sách bình luận của bài viết
     * @param postId ID của bài viết
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param includeAllStatuses Có bao gồm tất cả trạng thái không
     * @return PagedResponse chứa danh sách bình luận
     */
    @GetMapping("/post/{postId}")
    @Operation(
            summary = "Lấy bình luận theo bài viết", 
            description = "Trả về danh sách bình luận của một bài viết cụ thể")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách bình luận thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<PagedResponse<CommentDTO>> getCommentsByPost(
            @PathVariable UUID postId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Bao gồm các bình luận ở mọi trạng thái (chỉ ADMIN)") @RequestParam(defaultValue = "false") boolean includeAllStatuses) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId, pageNo, pageSize, includeAllStatuses));
    }

    /**
     * Lấy danh sách bình luận của tác giả
     * @param authorId ID của tác giả
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param includeAllStatuses Có bao gồm tất cả trạng thái không
     * @return PagedResponse chứa danh sách bình luận
     */
    @GetMapping("/author/{authorId}")
    @Operation(
            summary = "Lấy bình luận theo tác giả", 
            description = "Trả về danh sách bình luận của một tác giả cụ thể")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách bình luận thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<CommentDTO>> getCommentsByAuthor(
            @PathVariable String authorId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Bao gồm các bình luận ở mọi trạng thái (chỉ ADMIN)") @RequestParam(defaultValue = "false") boolean includeAllStatuses) {
        return ResponseEntity.ok(commentService.getCommentsByAuthor(authorId, pageNo, pageSize, includeAllStatuses));
    }

    /**
     * Lấy danh sách bình luận theo trạng thái
     * @param status Trạng thái bình luận
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse chứa danh sách bình luận
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Lấy bình luận theo trạng thái", 
            description = "Trả về danh sách bình luận theo trạng thái cụ thể (yêu cầu quyền ADMIN)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách bình luận thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class))),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện")
    })
    public ResponseEntity<PagedResponse<CommentDTO>> getCommentsByStatus(
            @PathVariable CommentStatus status,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize) {
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
    @Operation(
            summary = "Cập nhật trạng thái bình luận", 
            description = "Thay đổi trạng thái của bình luận (APPROVED, PENDING, REJECTED) (yêu cầu quyền ADMIN)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cập nhật trạng thái thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = CommentDTO.class))),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bình luận")
    })
    public ResponseEntity<CommentDTO> updateCommentStatus(
            @PathVariable UUID id,
            @Parameter(description = "Trạng thái mới: APPROVED, PENDING, REJECTED") @RequestParam CommentStatus status) {
        return ResponseEntity.ok(commentService.updateCommentStatus(id, status));
    }
} 