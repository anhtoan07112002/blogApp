package com.blogApp.blogpost.controller;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogpost.dto.CategoryDTO;
import com.blogApp.blogpost.service.interfaces.CategoryService;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller xử lý các yêu cầu liên quan đến danh mục
 * - Cung cấp các API CRUD cho danh mục
 * - Hỗ trợ phân trang và sắp xếp
 * - Tích hợp với caching để tối ưu hiệu suất
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "API quản lý danh mục")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Tạo danh mục mới
     * @param categoryDTO DTO chứa thông tin danh mục
     * @return CategoryDTO chứa thông tin danh mục đã tạo
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Tạo danh mục mới", 
            description = "Tạo một danh mục mới cho bài viết (yêu cầu quyền ADMIN)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tạo danh mục thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = CategoryDTO.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Dữ liệu không hợp lệ"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện")
    })
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }

    /**
     * Lấy danh mục theo ID
     * @param id ID của danh mục
     * @return CategoryDTO chứa thông tin danh mục
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy danh mục theo ID", 
            description = "Trả về thông tin chi tiết của danh mục theo ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh mục thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = CategoryDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Lấy danh mục theo slug
     * @param slug Slug của danh mục
     * @return CategoryDTO chứa thông tin danh mục
     */
    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Lấy danh mục theo slug", 
            description = "Trả về thông tin chi tiết của danh mục theo slug")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh mục thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = CategoryDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    /**
     * Cập nhật danh mục
     * @param id ID của danh mục
     * @param categoryDTO DTO chứa thông tin cập nhật
     * @return CategoryDTO chứa thông tin danh mục đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cập nhật danh mục", 
            description = "Cập nhật thông tin của danh mục theo ID (yêu cầu quyền ADMIN)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cập nhật danh mục thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = CategoryDTO.class))),
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
            description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    /**
     * Xóa danh mục
     * @param id ID của danh mục
     * @return Không có nội dung
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xóa danh mục", 
            description = "Xóa danh mục theo ID (yêu cầu quyền ADMIN)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Xóa danh mục thành công"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy tất cả danh mục
     * @return Danh sách CategoryDTO chứa thông tin tất cả danh mục
     */
    @GetMapping
    @Operation(
            summary = "Lấy tất cả danh mục", 
            description = "Trả về danh sách tất cả danh mục trong hệ thống")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách danh mục thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Lấy các danh mục gốc (không có danh mục cha)
     * @return Danh sách CategoryDTO chứa thông tin các danh mục gốc
     */
    @GetMapping("/root")
    @Operation(
            summary = "Lấy danh mục gốc", 
            description = "Trả về danh sách các danh mục gốc (không có danh mục cha)")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách danh mục gốc thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    /**
     * Lấy các danh mục con của một danh mục
     * @param parentId ID của danh mục cha
     * @return Danh sách CategoryDTO chứa thông tin các danh mục con
     */
    @GetMapping("/{parentId}/children")
    @Operation(
            summary = "Lấy danh mục con", 
            description = "Trả về danh sách các danh mục con của một danh mục cha")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách danh mục con thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = List.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy danh mục cha")
    })
    public ResponseEntity<List<CategoryDTO>> getSubCategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }

    /**
     * Lấy các danh mục phổ biến
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse chứa danh sách danh mục phổ biến
     */
    @GetMapping("/popular")
    @Operation(
            summary = "Lấy danh mục phổ biến", 
            description = "Trả về danh sách các danh mục phổ biến (có nhiều bài viết nhất)")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách danh mục phổ biến thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<CategoryDTO>> getPopularCategories(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(categoryService.getPopularCategories(pageNo, pageSize));
    }

    /**
     * Đếm số bài viết trong danh mục
     * @param id ID của danh mục
     * @return Số lượng bài viết trong danh mục
     */
    @GetMapping("/{id}/posts/count")
    @Operation(
            summary = "Đếm số bài viết trong danh mục", 
            description = "Trả về tổng số bài viết thuộc một danh mục cụ thể")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Đếm số bài viết thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Long.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<Long> countPostsInCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.countPostsInCategory(id));
    }
} 