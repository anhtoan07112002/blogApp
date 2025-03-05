package com.blogApp.blogpost.controller;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogpost.dto.CategoryDTO;
import com.blogApp.blogpost.service.interfaces.CategoryService;
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
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Tạo danh mục mới
     * @param categoryDTO DTO chứa thông tin danh mục
     * @return CategoryDTO chứa thông tin danh mục đã tạo
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Lấy danh mục theo slug
     * @param slug slug của danh mục
     * @return CategoryDTO chứa thông tin danh mục
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    /**
     * Cập nhật thông tin danh mục
     * @param id ID của danh mục cần cập nhật
     * @param categoryDTO DTO chứa thông tin cập nhật
     * @return CategoryDTO chứa thông tin danh mục đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    /**
     * Xóa danh mục
     * @param id ID của danh mục cần xóa
     * @return ResponseEntity không có nội dung
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy tất cả danh mục
     * @return List<CategoryDTO> chứa danh sách danh mục
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Lấy danh sách danh mục gốc
     * @return List<CategoryDTO> chứa danh sách danh mục gốc
     */
    @GetMapping("/root")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    /**
     * Lấy danh sách danh mục con
     * @param parentId ID của danh mục cha
     * @return List<CategoryDTO> chứa danh sách danh mục con
     */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<CategoryDTO>> getSubCategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }

    /**
     * Lấy danh sách danh mục phổ biến
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<CategoryDTO> chứa danh sách danh mục
     */
    @GetMapping("/popular")
    public ResponseEntity<PagedResponse<CategoryDTO>> getPopularCategories(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(categoryService.getPopularCategories(pageNo, pageSize));
    }

    /**
     * Đếm số bài viết trong danh mục
     * @param id ID của danh mục
     * @return Số lượng bài viết
     */
    @GetMapping("/{id}/posts/count")
    public ResponseEntity<Long> countPostsInCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.countPostsInCategory(id));
    }
} 