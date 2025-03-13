package com.blogApp.blogpost.dto.response;

import com.blogApp.blogcommon.dto.CategorySummaryDTO;
import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogcommon.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của bài viết
 * - Sử dụng cho trang chi tiết bài viết
 * - Chứa đầy đủ thông tin bao gồm nội dung
 * - Bao gồm danh sách bình luận
 * - Có thông tin về danh mục và tag
 * - Chứa các metadata như lượt xem, thời gian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết của bài viết")
public class PostDetailDTO {
    @Schema(description = "ID duy nhất của bài viết", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Tiêu đề bài viết", example = "Hướng dẫn sử dụng Spring Boot")
    private String title;
    
    @Schema(description = "Slug của bài viết dùng cho URL", example = "huong-dan-su-dung-spring-boot")
    private String slug;
    
    @Schema(description = "Tóm tắt ngắn về bài viết", example = "Hướng dẫn cách sử dụng Spring Boot để xây dựng ứng dụng web")
    private String summary;
    
    @Schema(description = "Nội dung bài viết (hỗ trợ Markdown)", example = "# Giới thiệu\nSpring Boot là...")
    private String content;
    
    @Schema(description = "ID của tác giả", example = "123e4567-e89b-12d3-a456-426614174001")
    private String authorId;
    
    @Schema(description = "Tên của tác giả", example = "Nguyễn Văn A")
    private String authorName;
    
    @Schema(description = "Trạng thái của bài viết", example = "PUBLISHED")
    private PostStatus status;
    
    @Schema(description = "Cho phép bình luận hay không", example = "true")
    private boolean commentEnabled;
    
    @Schema(description = "Số lượt xem bài viết", example = "1250")
    private Integer viewCount;
    
    @Schema(description = "Thời điểm bài viết được xuất bản")
    private LocalDateTime publishedAt;
    
    @Schema(description = "Thời điểm bài viết được tạo")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời điểm bài viết được cập nhật gần nhất")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Danh sách các danh mục của bài viết")
    private Set<CategorySummaryDTO> categories;
    
    @Schema(description = "Danh sách các thẻ của bài viết")
    private Set<TagDTO> tags;
    
    @Schema(description = "Danh sách các bình luận của bài viết")
    private Set<CommentDTO> comments;
}
