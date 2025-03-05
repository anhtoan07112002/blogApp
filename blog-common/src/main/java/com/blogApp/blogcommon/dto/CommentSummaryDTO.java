package com.blogApp.blogcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO chứa thông tin tóm tắt của bình luận, dùng cho thông báo hoặc hiển thị
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSummaryDTO {
    private UUID id;
    private String content;
    private String authorId;
    private String authorName;
    private UUID postId;
    private String postTitle;
    private String postSlug;
    private LocalDateTime createdAt;
}
