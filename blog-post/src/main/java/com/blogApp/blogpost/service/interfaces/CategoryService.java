package com.blogApp.blogpost.service.interfaces;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogpost.dto.CategoryDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface cho CategoryService
 * - Quản lý các thao tác CRUD với danh mục
 * - Hỗ trợ cấu trúc phân cấp (parent-child)
 * - Tích hợp caching để tối ưu hiệu suất
 */ 
public interface CategoryService {
    /**
     * Tạo danh mục mới
     * @param categoryDTO DTO chứa thông tin danh mục
     * @return CategoryDTO chứa thông tin danh mục đã tạo
     */
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    /**
     * Lấy danh mục theo ID
     * @param id ID của danh mục
     * @return CategoryDTO chứa thông tin danh mục
     */     
    CategoryDTO getCategoryById(UUID id);

    /**
     * Lấy danh mục theo slug
     * @param slug slug của danh mục
     * @return CategoryDTO chứa thông tin danh mục
     */
    CategoryDTO getCategoryBySlug(String slug);

    /**
     * Cập nhật thông tin danh mục
     * @param id ID của danh mục cần cập nhật
     * @param categoryDTO DTO chứa thông tin cập nhật
     * @return CategoryDTO chứa thông tin danh mục đã cập nhật
     */
    CategoryDTO updateCategory(UUID id, CategoryDTO categoryDTO);

    /**
     * Xóa danh mục
     * @param id ID của danh mục cần xóa
     */
    void deleteCategory(UUID id);

    /**
     * Lấy tất cả danh mục
     * @return List<CategoryDTO> chứa danh sách danh mục
     */
    List<CategoryDTO> getAllCategories();

    /**
     * Lấy danh sách danh mục gốc (không có parent)
     * @return List<CategoryDTO> chứa danh sách danh mục gốc
     */
    List<CategoryDTO> getRootCategories();

    /**
     * Lấy danh sách danh mục con
     * @param parentId ID của danh mục cha
     * @return List<CategoryDTO> chứa danh sách danh mục con
     */
    List<CategoryDTO> getSubCategories(UUID parentId);

    /**
     * Lấy danh sách danh mục phổ biến
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<CategoryDTO> chứa danh sách danh mục phổ biến
     */ 
    PagedResponse<CategoryDTO> getPopularCategories(int pageNo, int pageSize);

    /**
     * Đếm số bài viết trong danh mục
     * @param categoryId ID của danh mục
     * @return long số bài viết trong danh mục
     */ 
    long countPostsInCategory(UUID categoryId);
}