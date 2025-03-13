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
 * DTO dùng cho cập nhật bài viết
 * - Tất cả các trường đều là tùy chọn (có thể null)
 * - Chỉ cập nhật các trường không null
 * - Hỗ trợ thay đổi danh mục và tag
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin yêu cầu cập nhật bài viết")
public class PostUpdateRequest {
    @Size(min = 3, max = 255, message = "Tiêu đề phải có từ 3 đến 255 ký tự")
    @Schema(description = "Tiêu đề bài viết", example = "Hướng dẫn sử dụng Spring Boot [Cập nhật]")
    @NotBlank(message = "Tiêu đề không được trống")
    private String title;
    
    @Schema(description = "Nội dung bài viết (hỗ trợ Markdown)", example = "# Giới thiệu (đã cập nhật)\nSpring Boot là...")
    @NotBlank(message = "Nội dung không được trống")
    private String content;
    
    @Schema(description = "Tóm tắt ngắn về bài viết", example = "Hướng dẫn cập nhật về cách sử dụng Spring Boot")
    private String summary;
    
    @Schema(description = "Trạng thái của bài viết", example = "PUBLISHED")
    @NotNull(message = "Trạng thái không được trống")
    private PostStatus status;
    
    @Schema(description = "Cho phép bình luận hay không", example = "true")
    private Boolean commentEnabled;
    
    public boolean isCommentEnabled() {
        return commentEnabled != null && commentEnabled;
    }
    
    @Schema(description = "Danh sách ID của các danh mục", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
    private Set<UUID> categoryIds;
    
    @Schema(description = "Danh sách tên các thẻ", example = "[\"Spring Boot\", \"Java\"]")
    private Set<String> tags;
}