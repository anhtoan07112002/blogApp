package com.blogApp.blogpost.dto;

import com.blogApp.blogcommon.dto.CategorySummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin chi tiết của danh mục")
public class CategoryDTO {
    /**
     * ID duy nhất của danh mục
     */
    @Schema(description = "ID duy nhất của danh mục", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    /**
     * Tên danh mục
     * - Không được trống
     * - Độ dài từ 2-50 ký tự
     */
    @NotBlank(message = "Tên danh mục không được trống")
    @Size(min = 2, max = 50, message = "Tên danh mục phải có từ 2 đến 50 ký tự")
    @Schema(description = "Tên danh mục", example = "Công nghệ", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * Slug của danh mục, dùng cho URL
     */
    @Schema(description = "Slug của danh mục dùng cho URL", example = "cong-nghe")
    private String slug;

    /**
     * Mô tả chi tiết của danh mục
     */
    @Schema(description = "Mô tả chi tiết của danh mục", example = "Các bài viết về công nghệ, phần mềm và phần cứng")
    private String description;

    /**
     * ID của danh mục cha (nếu có)
     */
    @Schema(description = "ID của danh mục cha (nếu có)", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID parentId;

    /**
     * Tên của danh mục cha (nếu có)
     */
    @Schema(description = "Tên của danh mục cha (nếu có)", example = "Khoa học")
    private String parentName;

    /**
     * Danh sách các danh mục con
     */
    @Schema(description = "Danh sách các danh mục con")
    private Set<CategorySummaryDTO> children;

    /**
     * Số lượng bài viết trong danh mục
     */
    @Schema(description = "Số lượng bài viết trong danh mục", example = "15")
    private int postCount;

    /**
     * Thời điểm tạo danh mục
     */
    @Schema(description = "Thời điểm tạo danh mục")
    private LocalDateTime createdAt;

    /**
     * Thời điểm cập nhật danh mục
     */
    @Schema(description = "Thời điểm cập nhật danh mục gần nhất")
    private LocalDateTime updatedAt;
}
