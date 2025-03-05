package com.blogApp.blogpost.dto.response;

import com.blogApp.blogpost.model.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của bình luận
 * - Sử dụng cho hiển thị bình luận trong bài viết
 * - Hỗ trợ cấu trúc phân cấp (parent-child)
 * - Chứa thông tin người bình luận
 * - Bao gồm trạng thái và thời gian
 * - Có danh sách các bình luận trả lời
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private UUID id;
    private String content;
    private String authorId;
    private String authorName;
    private CommentStatus status;
    private UUID postId;
    private UUID parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<CommentDTO> replies;
}
