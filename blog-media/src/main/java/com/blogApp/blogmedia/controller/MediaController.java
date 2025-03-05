package com.blogApp.blogmedia.controller;

import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogmedia.dto.MediaDTO;
import com.blogApp.blogmedia.service.interfaces.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    /**
     * Upload một file media
     */
    @PostMapping("/upload")
    public ResponseEntity<MediaDTO> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") MediaFileType type) {
        return ResponseEntity.ok(mediaService.uploadMedia(file, type));
    }

    /**
     * Upload một file media với metadata
     */
    @PostMapping("/upload-with-metadata")
    public ResponseEntity<MediaDTO> uploadMediaWithMetadata(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") MediaFileType type,
            @RequestBody Map<String, String> metadata) {
        return ResponseEntity.ok(mediaService.uploadMediaWithMetadata(file, type, metadata));
    }

    /**
     * Lấy thông tin media theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MediaDTO> getMedia(@PathVariable String id) {
        return ResponseEntity.ok(mediaService.getMedia(id));
    }

    /**
     * Lấy nội dung media
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> getMediaContent(@PathVariable String id) {
        InputStream inputStream = mediaService.getMediaContent(id);
        MediaDTO mediaDTO = mediaService.getMedia(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaDTO.getContentType()));
        headers.setContentDispositionFormData("attachment", mediaDTO.getFileName());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }

    /**
     * Lấy URL của media
     */
    @GetMapping("/{id}/url")
    public ResponseEntity<String> getMediaUrl(@PathVariable String id) {
        return ResponseEntity.ok(mediaService.getMediaUrl(id));
    }

    /**
     * Xóa media
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cập nhật metadata của media
     */
    @PatchMapping("/{id}/metadata")
    public ResponseEntity<MediaDTO> updateMediaMetadata(
            @PathVariable String id,
            @RequestBody Map<String, String> metadata) {
        return ResponseEntity.ok(mediaService.updateMediaMetadata(id, metadata));
    }

    /**
     * Validate loại media
     */
    @GetMapping("/validate/type")
    public ResponseEntity<Boolean> validateMediaType(@RequestParam String mediaType) {
        return ResponseEntity.ok(mediaService.validateMediaType(mediaType));
    }

    /**
     * Validate kích thước media
     */
    @GetMapping("/validate/size")
    public ResponseEntity<Boolean> validateMediaSize(@RequestParam long size) {
        return ResponseEntity.ok(mediaService.validateMediaSize(size));
    }
} 