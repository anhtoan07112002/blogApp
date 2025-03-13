package com.blogApp.blogpost.dto.request;

import com.blogApp.blogcommon.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * DTO dùng cho yêu cầu tạo bài viết mới
 * - Chứa thông tin cơ bản của bài viết
 * - Có validation cho các trường bắt buộc
 * - Hỗ trợ gán danh mục và tags
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin yêu cầu tạo bài viết mới")
public class PostCreateRequest {
    @NotBlank(message = "Tiêu đề không được trống")
    @Size(min = 3, max = 255, message = "Tiêu đề phải có từ 3 đến 255 ký tự")
    @Schema(description = "Tiêu đề bài viết", example = "Hướng dẫn sử dụng Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Nội dung không được trống")
    @Schema(description = "Nội dung bài viết (hỗ trợ Markdown)", example = "# Giới thiệu\nSpring Boot là...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "Tóm tắt ngắn về bài viết", example = "Hướng dẫn cách sử dụng Spring Boot để xây dựng ứng dụng web")
    private String summary;

    @NotNull(message = "Trạng thái không được trống")
    @Schema(description = "Trạng thái của bài viết", example = "DRAFT", requiredMode = Schema.RequiredMode.REQUIRED)
    private PostStatus status;

    @Schema(description = "Cho phép bình luận hay không", example = "true", defaultValue = "true")
    private boolean commentEnabled = true;

    @Schema(description = "Danh sách ID của các danh mục", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
    private Set<UUID> categoryIds;

    @Schema(description = "Danh sách tên các thẻ", example = "[\"Spring Boot\", \"Java\"]")
    private Set<String> tags;
}

