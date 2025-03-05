package com.blogApp.blogcommon.dto;

import com.blogApp.blogcommon.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO chứa thông tin tóm tắt của bài viết, dùng cho hiển thị danh sách bài viết
 * hoặc khi các service khác cần tham chiếu đến bài viết
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDTO {
    private UUID id;
    private String title;
    private String slug;
    private String summary;
    private String authorId;
    private String authorName;
    private PostStatus status;
    private Integer viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private Set<TagDTO> tags;
    private Set<CategorySummaryDTO> categories;
}
