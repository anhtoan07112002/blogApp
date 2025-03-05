package com.blogApp.blogpost.service.impl;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogpost.dto.request.TagCreateRequest;
import com.blogApp.blogpost.mapper.TagMapper;
import com.blogApp.blogpost.model.Tag;
import com.blogApp.blogpost.repository.TagRepository;
import com.blogApp.blogpost.service.interfaces.TagService;
import com.blogApp.blogpost.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final CacheService cacheService;
    private final SlugUtils slugUtils;

    @Value("${app.service.name:post}")
    private String serviceName;

    @Value("${blog.post.cache.prefix:blog}")
    private String cachePrefix;

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private Long cacheTtl;

    private static final String CACHE_TYPE = "tags";

    private String getCacheKey(String key) {
        return String.format("%s:%s:%s:%s", cachePrefix, serviceName, CACHE_TYPE, key);
    }

    @Override
    @Transactional
    public TagDTO createTag(TagDTO tagDTO) {
        log.info("Bắt đầu tạo tag mới: {}", tagDTO.getName());

        // Tạo request từ DTO
        TagCreateRequest request = TagCreateRequest.builder()
                .name(tagDTO.getName())
                .build();

        // Tạo slug từ tên
        String slug = slugUtils.createSlug(request.getName());
        int counter = 1;
        while (tagRepository.existsBySlug(slug)) {
            slug = slugUtils.createSlug(request.getName()) + "-" + counter++;
        }

        // Tạo tag mới
        Tag tag = tagMapper.toEntity(request);
        tag.setSlug(slug);

        Tag savedTag = tagRepository.save(tag);
        log.info("Đã tạo thành công tag với id {}", savedTag.getId());

        // Xóa cache liên quan
        cacheService.delete(CACHE_TYPE, "all");
        cacheService.delete(CACHE_TYPE, "popular");

        return tagMapper.toDto(savedTag);
    }

    @Override
    public TagDTO getTagById(UUID id) {
        log.debug("Lấy thông tin tag {}", id);

        // Thử lấy từ cache trước
        String cacheKey = "id:" + id;
        Object cachedTag = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedTag != null) {
            log.debug("Lấy tag {} từ cache", id);
            return (TagDTO) cachedTag;
        }

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy tag với id {}", id);
                    return new IllegalStateException("Tag không tồn tại: " + id);
                });

        TagDTO tagDTO = tagMapper.toDto(tag);

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, tagDTO, cacheTtl / 1000, TimeUnit.SECONDS);

        return tagDTO;
    }

    @Override
    public TagDTO getTagBySlug(String slug) {
        log.debug("Lấy thông tin tag theo slug: {}", slug);

        // Thử lấy từ cache trước
        String cacheKey = "slug:" + slug;
        Object cachedTag = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedTag != null) {
            log.debug("Lấy tag với slug {} từ cache", slug);
            return (TagDTO) cachedTag;
        }

        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy tag với slug {}", slug);
                    return new IllegalStateException("Tag không tồn tại: " + slug);
                });

        TagDTO tagDTO = tagMapper.toDto(tag);

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, tagDTO, cacheTtl / 1000, TimeUnit.SECONDS);

        return tagDTO;
    }

    @Override
    @Transactional
    public TagDTO updateTag(UUID id, TagDTO tagDTO) {
        log.info("Bắt đầu cập nhật tag {}", id);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy tag với id {}", id);
                    return new IllegalStateException("Tag không tồn tại: " + id);
                });

        // Tạo request từ DTO
        TagCreateRequest request = TagCreateRequest.builder()
                .name(tagDTO.getName())
                .build();

        // Cập nhật tên
        tag.setName(request.getName());

        // Cập nhật slug nếu tên thay đổi
        if (!tag.getName().equals(request.getName())) {
            String newSlug = slugUtils.createSlug(request.getName());
            int counter = 1;
            while (tagRepository.existsBySlug(newSlug)) {
                newSlug = slugUtils.createSlug(request.getName()) + "-" + counter++;
            }
            tag.setSlug(newSlug);
        }

        Tag updatedTag = tagRepository.save(tag);
        log.info("Đã cập nhật thành công tag {}", id);

        // Xóa cache liên quan
        cacheService.delete(CACHE_TYPE, "id:" + id);
        cacheService.delete(CACHE_TYPE, "slug:" + tag.getSlug());
        cacheService.delete(CACHE_TYPE, "all");
        cacheService.delete(CACHE_TYPE, "popular");

        return tagMapper.toDto(updatedTag);
    }

    @Override
    @Transactional
    public void deleteTag(UUID id) {
        log.info("Bắt đầu xóa tag {}", id);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy tag với id {}", id);
                    return new IllegalStateException("Tag không tồn tại: " + id);
                });

        // Xóa cache trước khi xóa
        cacheService.delete(CACHE_TYPE, "id:" + id);
        cacheService.delete(CACHE_TYPE, "slug:" + tag.getSlug());
        cacheService.delete(CACHE_TYPE, "all");
        cacheService.delete(CACHE_TYPE, "popular");

        tagRepository.delete(tag);
        log.info("Đã xóa thành công tag {}", id);
    }

    @Override
    public List<TagDTO> getAllTags() {
        log.debug("Lấy tất cả tag");

        // Thử lấy từ cache trước
        Object cachedTags = cacheService.get(CACHE_TYPE, "all");
        if (cachedTags != null) {
            log.debug("Lấy danh sách tag từ cache");
            return (List<TagDTO>) cachedTags;
        }

        List<Tag> tags = tagRepository.findAll();
        List<TagDTO> tagDTOs = tags.stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, "all", tagDTOs, cacheTtl / 1000, TimeUnit.SECONDS);

        return tagDTOs;
    }

    @Override
    public PagedResponse<TagDTO> getPopularTags(int pageNo, int pageSize) {
        log.debug("Lấy danh sách tag phổ biến - trang {}, kích thước {}", pageNo, pageSize);

        // Thử lấy từ cache trước
        String cacheKey = "popular:page:" + pageNo + ":size:" + pageSize;
        Object cachedResponse = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách tag phổ biến từ cache");
            return (PagedResponse<TagDTO>) cachedResponse;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Tag> tagPage = tagRepository.findPopularTags(pageable);

        List<TagDTO> tagDTOs = tagPage.getContent().stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());

        PagedResponse<TagDTO> response = PagedResponse.<TagDTO>builder()
                .content(tagDTOs)
                .pageNo(tagPage.getNumber())
                .size(tagPage.getSize())
                .totalElements(tagPage.getTotalElements())
                .totalPages(tagPage.getTotalPages())
                .last(tagPage.isLast())
                .build();

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);

        return response;
    }
} 