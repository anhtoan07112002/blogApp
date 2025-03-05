package com.blogApp.blogpost.dto.response;

import com.blogApp.blogcommon.dto.CategorySummaryDTO;
import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogcommon.enums.PostStatus;
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
public class PostDetailDTO {
    private UUID id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String authorId;
    private String authorName;
    private PostStatus status;
    private boolean commentEnabled;
    private Integer viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<CategorySummaryDTO> categories;
    private Set<TagDTO> tags;
    private Set<CommentDTO> comments;
}
