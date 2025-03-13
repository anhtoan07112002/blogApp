package com.blogApp.blogpost.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO dùng cho yêu cầu tạo bình luận mới
 * - Chứa thông tin cơ bản của bình luận
 * - Có validation cho nội dung và bài viết
 * - Hỗ trợ cả bình luận mới và phản hồi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin yêu cầu tạo bình luận mới")
public class CommentCreateRequest {
    @NotBlank(message = "Nội dung bình luận không được trống")
    @Schema(description = "Nội dung bình luận", example = "Bài viết rất hay và bổ ích!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    
    @Schema(description = "ID của bài viết được bình luận", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID postId;
    
    @Schema(description = "ID của bình luận cha (nếu đây là bình luận phản hồi)", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID parentId;
}
