package com.blogApp.blogpost.controller;

import com.blogApp.blogcommon.dto.UserPrincipal;
import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.enums.PostStatus;
import com.blogApp.blogpost.dto.request.PostCreateRequest;
import com.blogApp.blogpost.dto.request.PostUpdateRequest;
import com.blogApp.blogpost.dto.response.PostSummaryDTO;
import com.blogApp.blogpost.service.interfaces.PostService;

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
 * Controller xử lý các yêu cầu liên quan đến bài viết
 * - Cung cấp các API CRUD cho bài viết
 * - Hỗ trợ phân trang và sắp xếp
 * - Tích hợp với caching để tối ưu hiệu suất
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "API quản lý bài viết")
public class PostController {

    private final PostService postService;

    /**
     * Tạo bài viết mới
     * @param request DTO chứa thông tin bài viết
     * @param userPrincipal Thông tin người dùng đã xác thực
     * @return PostSummaryDTO chứa thông tin bài viết đã tạo
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Tạo bài viết mới", 
            description = "Tạo một bài viết mới với nội dung và thông tin đính kèm",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tạo bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PostSummaryDTO.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Dữ liệu không hợp lệ"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập")
    })
    public ResponseEntity<PostSummaryDTO> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(postService.createPost(request, userPrincipal.getId().toString()));
    }

    /**
     * Lấy bài viết theo ID
     * @param id ID của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy bài viết theo ID", 
            description = "Trả về thông tin chi tiết của bài viết theo ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PostSummaryDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<PostSummaryDTO> getPostById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    /**
     * Lấy bài viết theo slug
     * @param slug Slug của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết
     */
    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Lấy bài viết theo slug", 
            description = "Trả về thông tin chi tiết của bài viết theo slug")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PostSummaryDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<PostSummaryDTO> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }

    /**
     * Cập nhật bài viết
     * @param id ID của bài viết
     * @param request DTO chứa thông tin cập nhật
     * @param userPrincipal Thông tin người dùng đã xác thực
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cập nhật bài viết", 
            description = "Cập nhật thông tin của bài viết theo ID",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cập nhật bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PostSummaryDTO.class))),
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
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<PostSummaryDTO> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody PostUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(postService.updatePost(id, request, userPrincipal.getId().toString()));
    }

    /**
     * Xóa bài viết
     * @param id ID của bài viết
     * @param userPrincipal Thông tin người dùng đã xác thực
     * @return Không có nội dung
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Xóa bài viết", 
            description = "Xóa bài viết theo ID",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Xóa bài viết thành công"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.deletePost(id, userPrincipal.getId().toString());
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy danh sách bài viết theo phân trang
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Sắp xếp theo trường nào
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse chứa danh sách bài viết
     */
    @GetMapping
    @Operation(
            summary = "Lấy danh sách bài viết",
            description = "Trả về danh sách bài viết có phân trang và sắp xếp")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<PostSummaryDTO>> getAllPosts(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Sắp xếp theo trường") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp: asc hoặc desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(postService.getAllPosts(pageNo, pageSize, sortBy, sortDir));
    }

    /**
     * Lấy danh sách bài viết của tác giả
     * @param authorId ID của tác giả
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Sắp xếp theo trường nào
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse chứa danh sách bài viết
     */
    @GetMapping("/author/{authorId}")
    @Operation(
            summary = "Lấy danh sách bài viết của tác giả", 
            description = "Trả về danh sách bài viết của một tác giả cụ thể")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<PostSummaryDTO>> getPostsByAuthor(
            @PathVariable String authorId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Sắp xếp theo trường") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp: asc hoặc desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId, pageNo, pageSize, sortBy, sortDir));
    }

    /**
     * Lấy danh sách bài viết của thẻ (tag)
     * @param tagId ID của thẻ
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Sắp xếp theo trường nào
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse chứa danh sách bài viết
     */
    @GetMapping("/tag/{tagId}")
    @Operation(
            summary = "Lấy danh sách bài viết theo thẻ (tag)", 
            description = "Trả về danh sách bài viết có gắn một thẻ cụ thể")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<PostSummaryDTO>> getPostsByTag(
            @PathVariable UUID tagId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Sắp xếp theo trường") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp: asc hoặc desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(postService.getPostsByTag(tagId, pageNo, pageSize, sortBy, sortDir));
    }

    /**
     * Tìm kiếm bài viết
     * @param keyword Từ khóa tìm kiếm
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse chứa danh sách bài viết
     */
    @GetMapping("/search")
    @Operation(
            summary = "Tìm kiếm bài viết", 
            description = "Tìm kiếm bài viết theo từ khóa trong tiêu đề và nội dung")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tìm kiếm bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<PostSummaryDTO>> searchPosts(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(postService.searchPosts(keyword, pageNo, pageSize));
    }

    /**
     * Cập nhật trạng thái bài viết
     * @param id ID của bài viết
     * @param status Trạng thái mới
     * @param userPrincipal Thông tin người dùng đã xác thực
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cập nhật trạng thái bài viết", 
            description = "Thay đổi trạng thái của bài viết (DRAFT, PUBLISHED, ARCHIVED)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cập nhật trạng thái thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PostSummaryDTO.class))),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<PostSummaryDTO> updatePostStatus(
            @PathVariable UUID id,
            @Parameter(description = "Trạng thái mới: DRAFT, PUBLISHED, ARCHIVED") @RequestParam PostStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(postService.updatePostStatus(id, status, userPrincipal.getId().toString()));
    }

    /**
     * Tăng lượt xem bài viết
     * @param id ID của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */
    @PostMapping("/{id}/view")
    @Operation(
            summary = "Tăng lượt xem bài viết", 
            description = "Tăng số lượt xem của bài viết lên 1")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tăng lượt xem thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PostSummaryDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<PostSummaryDTO> incrementViewCount(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.incrementViewCount(id));
    }
} 