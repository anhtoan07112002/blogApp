package com.blogApp.blogmedia.controller;

import com.blogApp.blogmedia.dto.MediaDTO;
import com.blogApp.blogmedia.service.interfaces.MediaPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media/post")
@RequiredArgsConstructor
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
    public ResponseEntity<Void> addMediaToPost(
            @PathVariable UUID mediaId,
            @PathVariable UUID postId,
            @RequestParam String type,
            @RequestParam Integer position) {
        mediaPostService.addMediaToPost(mediaId, postId, type, position);
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa media khỏi post
     * @param mediaId ID của media
     * @param postId ID của post
     */
    @DeleteMapping("/{mediaId}/post/{postId}")
    public ResponseEntity<Void> removeMediaFromPost(
            @PathVariable UUID mediaId,
            @PathVariable UUID postId) {
        mediaPostService.removeMediaFromPost(mediaId, postId);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy danh sách media của post
     * @param postId ID của post
     * @return Danh sách media
     */
    @GetMapping("/{postId}")
    public ResponseEntity<List<MediaDTO>> getPostMedia(@PathVariable UUID postId) {
        return ResponseEntity.ok(mediaPostService.getPostMedia(postId));
    }
} 