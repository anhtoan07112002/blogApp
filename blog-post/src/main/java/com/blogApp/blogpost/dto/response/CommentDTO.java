package com.blogApp.blogpost.dto.response;

import com.blogApp.blogpost.model.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin bình luận của bài viết")
public class CommentDTO {
    @Schema(description = "ID duy nhất của bình luận", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "ID của bài viết được bình luận", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID postId;
    
    @Schema(description = "ID của người bình luận", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID userId;
    
    @Schema(description = "Tên người bình luận", example = "Nguyễn Văn A")
    private String userName;
    
    @Schema(description = "Nội dung bình luận", example = "Bài viết rất hay và bổ ích!")
    private String content;
    
    @Schema(description = "ID của bình luận cha (nếu đây là bình luận phản hồi)", example = "123e4567-e89b-12d3-a456-426614174003")
    private UUID parentId;
    
    @Schema(description = "Thời điểm bình luận được tạo")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời điểm bình luận được cập nhật gần nhất")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Danh sách các bình luận phản hồi")
    private Set<CommentDTO> replies;
}
