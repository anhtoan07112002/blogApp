package com.blogApp.blogmedia.controller;

import com.blogApp.blogcommon.enums.MediaFileType;
import com.blogApp.blogmedia.dto.MediaDTO;
import com.blogApp.blogmedia.service.interfaces.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Media", description = "API quản lý tệp media như hình ảnh, video, âm thanh, tài liệu")
public class MediaController {

    private final MediaService mediaService;

    /**
     * Upload một file media
     */
    @PostMapping("/upload")
    @Operation(
            summary = "Upload file media", 
            description = "Tải lên một file media (hình ảnh, video, audio, tài liệu)")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Upload thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = MediaDTO.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "File không hợp lệ hoặc vượt quá kích thước cho phép")
    })
    public ResponseEntity<MediaDTO> uploadMedia(
            @Parameter(description = "File cần upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Loại file: IMAGE, VIDEO, AUDIO, DOCUMENT") @RequestParam("type") MediaFileType type) {
        return ResponseEntity.ok(mediaService.uploadMedia(file, type));
    }

    /**
     * Upload một file media với metadata
     */
    @PostMapping("/upload-with-metadata")
    @Operation(
            summary = "Upload file media kèm metadata", 
            description = "Tải lên một file media với các thông tin metadata bổ sung")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Upload thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = MediaDTO.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "File không hợp lệ hoặc vượt quá kích thước cho phép")
    })
    public ResponseEntity<MediaDTO> uploadMediaWithMetadata(
            @Parameter(description = "File cần upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Loại file: IMAGE, VIDEO, AUDIO, DOCUMENT") @RequestParam("type") MediaFileType type,
            @Parameter(description = "Metadata bổ sung cho file") @RequestBody Map<String, String> metadata) {
        return ResponseEntity.ok(mediaService.uploadMediaWithMetadata(file, type, metadata));
    }

    /**
     * Lấy thông tin media theo ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy thông tin media", 
            description = "Lấy thông tin chi tiết của một file media theo ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy thông tin thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = MediaDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy media")
    })
    public ResponseEntity<MediaDTO> getMedia(@PathVariable String id) {
        return ResponseEntity.ok(mediaService.getMedia(id));
    }

    /**
     * Lấy nội dung file media
     */
    @GetMapping("/{id}/content")
    @Operation(
            summary = "Tải xuống file media", 
            description = "Tải xuống nội dung của file media theo ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Tải xuống thành công"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy media")
    })
    public ResponseEntity<InputStreamResource> getMediaContent(@PathVariable String id) {
        MediaDTO mediaDTO = mediaService.getMedia(id);
        InputStream inputStream = mediaService.getMediaContent(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaDTO.getContentType()));
        headers.setContentDispositionFormData("attachment", mediaDTO.getFileName());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }

    /**
     * Lấy URL của file media
     */
    @GetMapping("/{id}/url")
    @Operation(
            summary = "Lấy URL của media", 
            description = "Lấy đường dẫn URL để truy cập file media")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy URL thành công",
            content = @Content(mediaType = "text/plain", 
                schema = @Schema(type = "string"))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy media")
    })
    public ResponseEntity<String> getMediaUrl(@PathVariable String id) {
        return ResponseEntity.ok(mediaService.getMediaUrl(id));
    }

    /**
     * Xóa file media
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xóa file media", 
            description = "Xóa file media theo ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Xóa thành công"),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy media")
    })
    public ResponseEntity<Void> deleteMedia(@PathVariable String id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cập nhật metadata của media
     */
    @PatchMapping("/{id}/metadata")
    @Operation(
            summary = "Cập nhật metadata", 
            description = "Cập nhật thông tin metadata cho file media")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cập nhật thành công",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = MediaDTO.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy media")
    })
    public ResponseEntity<MediaDTO> updateMediaMetadata(
            @PathVariable String id,
            @RequestBody Map<String, String> metadata) {
        return ResponseEntity.ok(mediaService.updateMediaMetadata(id, metadata));
    }

    /**
     * Kiểm tra tính hợp lệ của loại media
     */
    @GetMapping("/validate/type")
    @Operation(
            summary = "Kiểm tra loại media", 
            description = "Kiểm tra tính hợp lệ của một loại media (MIME type)")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Kết quả kiểm tra",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(type = "boolean")))
    })
    public ResponseEntity<Boolean> validateMediaType(@RequestParam String mediaType) {
        return ResponseEntity.ok(mediaService.validateMediaType(mediaType));
    }

    /**
     * Kiểm tra tính hợp lệ của kích thước media
     */
    @GetMapping("/validate/size")
    @Operation(
            summary = "Kiểm tra kích thước media", 
            description = "Kiểm tra tính hợp lệ của kích thước file media")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Kết quả kiểm tra",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(type = "boolean")))
    })
    public ResponseEntity<Boolean> validateMediaSize(@RequestParam long size) {
        return ResponseEntity.ok(mediaService.validateMediaSize(size));
    }
} 