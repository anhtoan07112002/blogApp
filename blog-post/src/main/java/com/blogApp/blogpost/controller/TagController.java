package com.blogApp.blogpost.controller;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogpost.service.interfaces.TagService;
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
public class TagController {

    private final TagService tagService;

    /**
     * Tạo tag mới
     * @param tagDTO DTO chứa thông tin tag
     * @return TagDTO chứa thông tin tag đã tạo
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<TagDTO> getTagById(@PathVariable UUID id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    /**
     * Lấy tag theo slug
     * @param slug slug của tag
     * @return TagDTO chứa thông tin tag
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<TagDTO> getTagBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(tagService.getTagBySlug(slug));
    }

    /**
     * Cập nhật thông tin tag
     * @param id ID của tag cần cập nhật
     * @param tagDTO DTO chứa thông tin cập nhật
     * @return TagDTO chứa thông tin tag đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody TagDTO tagDTO) {
        return ResponseEntity.ok(tagService.updateTag(id, tagDTO));
    }

    /**
     * Xóa tag
     * @param id ID của tag cần xóa
     * @return ResponseEntity không có nội dung
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy tất cả tag
     * @return List<TagDTO> chứa danh sách tag
     */
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    /**
     * Lấy danh sách tag phổ biến
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<TagDTO> chứa danh sách tag
     */
    @GetMapping("/popular")
    public ResponseEntity<PagedResponse<TagDTO>> getPopularTags(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(tagService.getPopularTags(pageNo, pageSize));
    }
} 