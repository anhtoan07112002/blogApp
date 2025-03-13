package com.blogApp.blogpost.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dùng cho yêu cầu tạo tag mới
 * - Chứa thông tin cơ bản của tag
 * - Có validation cho tên tag
 * - Tự động tạo slug từ tên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin yêu cầu tạo thẻ mới")
public class TagCreateRequest {
    @NotBlank(message = "Tên tag không được trống")
    @Size(min = 2, max = 30, message = "Tên tag phải có từ 2 đến 30 ký tự")
    @Schema(description = "Tên của thẻ", example = "Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
