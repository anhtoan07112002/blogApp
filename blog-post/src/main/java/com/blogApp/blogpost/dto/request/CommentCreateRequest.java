package com.blogApp.blogpost.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO dùng cho yêu cầu tạo bình luận mới
 * - Chứa nội dung bình luận
 * - Có validation cho nội dung bắt buộc
 * - Hỗ trợ bình luận trả lời (parentId)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    @NotBlank(message = "Nội dung không được trống")
    private String content;

    private UUID parentId; // ID của bình luận cha (nếu là reply)
}
