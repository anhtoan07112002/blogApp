package com.blogApp.blogpost.dto.filter;

import com.blogApp.blogcommon.dto.request.PageRequest;
import com.blogApp.blogcommon.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO dùng cho việc lọc bài viết
 * - Kế thừa từ PageRequest để hỗ trợ phân trang
 * - Cho phép lọc theo nhiều tiêu chí: từ khóa, danh mục, tag, tác giả
 * - Hỗ trợ lọc theo trạng thái và khoảng thời gian
 * - Sử dụng trong các API tìm kiếm và lọc bài viết
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostFilterRequest extends PageRequest {
    private String keyword;
    private Set<UUID> categoryIds;
    private Set<String> tags;
    private String authorId;
    private Set<PostStatus> statuses;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}