package com.blogApp.blogpost.dto.request;

import com.blogApp.blogcommon.enums.PostStatus;
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
public class PostCreateRequest {
    @NotBlank(message = "Tiêu đề không được trống")
    @Size(min = 3, max = 255, message = "Tiêu đề phải có từ 3 đến 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được trống")
    private String content;

    private String summary;

    @NotNull(message = "Trạng thái không được trống")
    private PostStatus status;

    private boolean commentEnabled = true;

    private Set<UUID> categoryIds;

    private Set<String> tags;
}

