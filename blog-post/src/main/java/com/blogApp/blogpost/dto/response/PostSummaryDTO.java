package com.blogApp.blogpost.dto.response;

import com.blogApp.blogcommon.dto.CategorySummaryDTO;
import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogcommon.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO chứa thông tin tóm tắt của bài viết, dùng cho hiển thị danh sách
 * - Không bao gồm nội dung đầy đủ
 * - Chỉ chứa thông tin cơ bản và tóm tắt
 * - Có thông tin về danh mục và tag
 * - Bao gồm số lượng bình luận thay vì danh sách đầy đủ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt của bài viết dùng cho hiển thị danh sách")
public class PostSummaryDTO {
    @Schema(description = "ID duy nhất của bài viết", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Tiêu đề bài viết", example = "Hướng dẫn sử dụng Spring Boot")
    @NotBlank(message = "Title is required")
    private String title;
    
    @Schema(description = "Slug của bài viết dùng cho URL", example = "huong-dan-su-dung-spring-boot")
    private String slug;
    
    @Schema(description = "Tóm tắt ngắn về bài viết", example = "Hướng dẫn cách sử dụng Spring Boot để xây dựng ứng dụng web")
    private String summary;
    
    @Schema(description = "ID của tác giả", example = "123e4567-e89b-12d3-a456-426614174001")
    private String authorId;
    
    @Schema(description = "Tên của tác giả", example = "Nguyễn Văn A")
    private String authorName;
    
    @Schema(description = "Trạng thái của bài viết", example = "PUBLISHED")
    @NotNull(message = "Status is required")
    private PostStatus status;
    
    @Schema(description = "URL ảnh đại diện của bài viết", example = "https://example.com/images/spring-boot.jpg")
    private String featuredImage;
    
    @Schema(description = "Số lượt xem bài viết", example = "1250")
    private Integer viewCount;
    
    @Schema(description = "Số lượng bình luận của bài viết", example = "15")
    private Integer commentCount;
    
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
}
