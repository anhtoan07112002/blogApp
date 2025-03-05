package com.blogApp.blogpost.dto;

import com.blogApp.blogcommon.dto.CategorySummaryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của danh mục, dùng cho hiển thị chi tiết
 * - Bao gồm thông tin cơ bản và các mối quan hệ
 * - Hỗ trợ cấu trúc phân cấp (parent-child)
 * - Chứa thông tin về số lượng bài viết
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    /**
     * ID duy nhất của danh mục
     */
    private UUID id;

    /**
     * Tên danh mục
     * - Không được trống
     * - Độ dài từ 2-50 ký tự
     */
    @NotBlank(message = "Tên danh mục không được trống")
    @Size(min = 2, max = 50, message = "Tên danh mục phải có từ 2 đến 50 ký tự")
    private String name;

    /**
     * Slug của danh mục, dùng cho URL
     */
    private String slug;

    /**
     * Mô tả chi tiết của danh mục
     */
    private String description;

    /**
     * ID của danh mục cha (nếu có)
     */
    private UUID parentId;

    /**
     * Tên của danh mục cha (nếu có)
     */
    private String parentName;

    /**
     * Danh sách các danh mục con
     */
    private Set<CategorySummaryDTO> children;

    /**
     * Số lượng bài viết trong danh mục
     */
    private int postCount;

    /**
     * Thời điểm tạo danh mục
     */
    private LocalDateTime createdAt;

    /**
     * Thời điểm cập nhật danh mục
     */
    private LocalDateTime updatedAt;
}
