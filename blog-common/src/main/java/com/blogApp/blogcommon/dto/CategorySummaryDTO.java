package com.blogApp.blogcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO chứa thông tin tóm tắt của danh mục, dùng cho hiển thị và tham chiếu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDTO {
    private UUID id;
    private String name;
    private String slug;
}