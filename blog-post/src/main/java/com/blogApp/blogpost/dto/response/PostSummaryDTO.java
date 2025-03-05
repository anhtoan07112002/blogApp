package com.blogApp.blogpost.dto.response;

import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogcommon.enums.PostStatus;
import com.blogApp.blogpost.dto.CategoryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO chứa thông tin tóm tắt của bài viết
 * - Sử dụng cho danh sách bài viết và preview
 * - Chứa các thông tin cơ bản: tiêu đề, tóm tắt, tác giả
 * - Bao gồm thông tin về danh mục, tag và số lượng comment
 * - Có các trường thời gian: tạo, cập nhật, xuất bản
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDTO {

    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    private String slug;

    private String summary;

    @NotBlank(message = "Content is required")
    private String content;

    private String authorId;

    private String authorName;

    @NotNull(message = "Status is required")
    private PostStatus status;

    private boolean commentEnabled;

    private Integer viewCount;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<CategoryDTO> categories;

    private Set<TagDTO> tags;

    private Long commentCount;
}
