package com.blogApp.blogpost.controller;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogpost.service.interfaces.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller xử lý các yêu cầu liên quan đến tag
 * - Cung cấp các API CRUD cho tag
 * - Hỗ trợ phân trang và sắp xếp
 * - Tích hợp với caching để tối ưu hiệu suất
 */
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "API quản lý thẻ (tag)")
public class TagController {

    private final TagService tagService;

    /**
     * Tạo tag mới
     * @param tagDTO DTO chứa thông tin tag
     * @return TagDTO chứa thông tin tag đã tạo
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Tạo thẻ mới", 
            description = "Tạo một thẻ mới cho bài viết (yêu cầu quyền ADMIN)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tạo thẻ thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TagDTO.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Dữ liệu không hợp lệ"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện")
    })
    public ResponseEntity<TagDTO> createTag(
            @Valid @RequestBody TagDTO tagDTO) {
        return ResponseEntity.ok(tagService.createTag(tagDTO));
    }

    /**
     * Lấy tag theo ID
     * @param id ID của tag
     * @return TagDTO chứa thông tin tag
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy thẻ theo ID", 
            description = "Trả về thông tin chi tiết của thẻ theo ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy thẻ thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TagDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy thẻ")
    })
    public ResponseEntity<TagDTO> getTagById(@PathVariable UUID id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    /**
     * Lấy tag theo slug
     * @param slug Slug của tag
     * @return TagDTO chứa thông tin tag
     */
    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Lấy thẻ theo slug", 
            description = "Trả về thông tin chi tiết của thẻ theo slug")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy thẻ thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TagDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy thẻ")
    })
    public ResponseEntity<TagDTO> getTagBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(tagService.getTagBySlug(slug));
    }

    /**
     * Cập nhật tag
     * @param id ID của tag
     * @param tagDTO DTO chứa thông tin cập nhật
     * @return TagDTO chứa thông tin tag đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cập nhật thẻ", 
            description = "Cập nhật thông tin của thẻ theo ID (yêu cầu quyền ADMIN)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cập nhật thẻ thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TagDTO.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Dữ liệu không hợp lệ"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy thẻ")
    })
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody TagDTO tagDTO) {
        return ResponseEntity.ok(tagService.updateTag(id, tagDTO));
    }

    /**
     * Xóa tag
     * @param id ID của tag
     * @return Không có nội dung
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xóa thẻ", 
            description = "Xóa thẻ theo ID (yêu cầu quyền ADMIN)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Xóa thẻ thành công"),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa đăng nhập"),
        @ApiResponse(
            responseCode = "403", 
            description = "Không có quyền thực hiện"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy thẻ")
    })
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy tất cả thẻ
     * @return Danh sách TagDTO chứa thông tin tất cả thẻ
     */
    @GetMapping
    @Operation(
            summary = "Lấy tất cả thẻ", 
            description = "Trả về danh sách tất cả thẻ trong hệ thống")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách thẻ thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    /**
     * Lấy các thẻ phổ biến
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse chứa danh sách thẻ phổ biến
     */
    @GetMapping("/popular")
    @Operation(
            summary = "Lấy thẻ phổ biến", 
            description = "Trả về danh sách các thẻ phổ biến (được sử dụng nhiều nhất)")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách thẻ phổ biến thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<TagDTO>> getPopularTags(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int pageNo,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(tagService.getPopularTags(pageNo, pageSize));
    }
} 