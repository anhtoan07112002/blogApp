package com.blogApp.blogmedia.controller;

import com.blogApp.blogmedia.dto.MediaDTO;
import com.blogApp.blogmedia.service.interfaces.MediaPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media/post")
@RequiredArgsConstructor
@Tag(name = "Media-Post", description = "API quản lý liên kết giữa media và bài viết")
public class MediaPostController {

    private final MediaPostService mediaPostService;

    /**
     * Thêm media vào post
     * @param mediaId ID của media
     * @param postId ID của post
     * @param type Loại media trong post (ví dụ: thumbnail, cover, content)
     * @param position Vị trí media trong post
     */
    @PostMapping("/{mediaId}/post/{postId}")
    @Operation(
            summary = "Thêm media vào bài viết", 
            description = "Thêm một file media vào bài viết với vị trí và loại cụ thể")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Thêm media thành công"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy media hoặc bài viết")
    })
    public ResponseEntity<Void> addMediaToPost(
            @Parameter(description = "ID của media") @PathVariable UUID mediaId,
            @Parameter(description = "ID của bài viết") @PathVariable UUID postId,
            @Parameter(description = "Loại media trong bài viết (thumbnail, cover, content)") @RequestParam String type,
            @Parameter(description = "Vị trí của media trong bài viết") @RequestParam Integer position) {
        mediaPostService.addMediaToPost(mediaId, postId, type, position);
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa media khỏi post
     * @param mediaId ID của media
     * @param postId ID của post
     */
    @DeleteMapping("/{mediaId}/post/{postId}")
    @Operation(
            summary = "Xóa media khỏi bài viết", 
            description = "Xóa liên kết giữa một file media và bài viết")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Xóa media thành công"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy liên kết giữa media và bài viết")
    })
    public ResponseEntity<Void> removeMediaFromPost(
            @Parameter(description = "ID của media") @PathVariable UUID mediaId,
            @Parameter(description = "ID của bài viết") @PathVariable UUID postId) {
        mediaPostService.removeMediaFromPost(mediaId, postId);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy danh sách media của post
     * @param postId ID của post
     * @return Danh sách MediaDTO chứa thông tin các media của post
     */
    @GetMapping("/{postId}")
    @Operation(
            summary = "Lấy danh sách media của bài viết", 
            description = "Lấy danh sách tất cả file media được gắn với một bài viết")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = MediaDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy bài viết")
    })
    public ResponseEntity<List<MediaDTO>> getPostMedia(
            @Parameter(description = "ID của bài viết") @PathVariable UUID postId) {
        return ResponseEntity.ok(mediaPostService.getPostMedia(postId));
    }
} 