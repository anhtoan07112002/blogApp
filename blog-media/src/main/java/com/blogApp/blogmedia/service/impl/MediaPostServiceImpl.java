package com.blogApp.blogmedia.service.impl;

import com.blogApp.blogmedia.dto.MediaDTO;
import com.blogApp.blogmedia.exception.MediaProcessingException;
import com.blogApp.blogmedia.model.MediaPost;
import com.blogApp.blogmedia.repository.MediaPostRepository;
import com.blogApp.blogmedia.service.interfaces.MediaPostService;
import com.blogApp.blogmedia.service.interfaces.MediaService;
import com.blogApp.blogcommon.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaPostServiceImpl implements MediaPostService {

    private final MediaPostRepository mediaPostRepository;
    private final MediaService mediaService;
    private final CacheService cacheService;

    private static final String CACHE_TYPE = "media_post";
    private static final long CACHE_TTL = 24; // 24 giờ
    private static final TimeUnit CACHE_TTL_UNIT = TimeUnit.HOURS;

    @Override
    @Transactional
    public void addMediaToPost(UUID mediaId, UUID postId, String type, Integer position) {
        try {
            // Kiểm tra media tồn tại
            mediaService.getMedia(mediaId.toString());
            
            // Tạo quan hệ mới
            MediaPost mediaPost = MediaPost.builder()
                    .mediaId(mediaId)
                    .postId(postId)
                    .type(type)
                    .position(position)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            mediaPostRepository.save(mediaPost);
            
            // Xóa cache của post
            cacheService.delete(CACHE_TYPE, postId.toString());
        } catch (Exception e) {
            log.error("Lỗi thêm media vào post: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể thêm media vào post", e);
        }
    }

    @Override
    @Transactional
    public void removeMediaFromPost(UUID mediaId, UUID postId) {
        try {
            mediaPostRepository.findByMediaIdAndPostId(mediaId, postId)
                    .ifPresent(mediaPostRepository::delete);
                    
            // Xóa cache của post
            cacheService.delete(CACHE_TYPE, postId.toString());
        } catch (Exception e) {
            log.error("Lỗi xóa media khỏi post: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể xóa media khỏi post", e);
        }
    }

    @Override
    public List<MediaDTO> getPostMedia(UUID postId) {
        try {
            // Kiểm tra cache
            String cacheKey = postId.toString();
            if (cacheService.hasKey(CACHE_TYPE, cacheKey)) {
                @SuppressWarnings("unchecked")
                List<MediaDTO> cachedMedia = (List<MediaDTO>) cacheService.get(CACHE_TYPE, cacheKey);
                return cachedMedia;
            }

            // Lấy từ database nếu không có trong cache
            List<MediaPost> mediaPosts = mediaPostRepository.findByPostId(postId);
            List<MediaDTO> mediaList = mediaPosts.stream()
                    .map(mediaPost -> mediaService.getMedia(mediaPost.getMediaId().toString()))
                    .toList();

            // Lưu vào cache
            cacheService.set(CACHE_TYPE, cacheKey, mediaList, CACHE_TTL, CACHE_TTL_UNIT);
            
            return mediaList;
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách media của post: {}", e.getMessage(), e);
            throw new MediaProcessingException("Không thể lấy danh sách media của post", e);
        }
    }
}
