package com.blogApp.blogpost.service.impl;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogpost.dto.CategoryDTO;
import com.blogApp.blogpost.exception.CategoryNotFoundException;
import com.blogApp.blogpost.mapper.CategoryMapper;
import com.blogApp.blogpost.model.Category;
import com.blogApp.blogpost.repository.CategoryRepository;
import com.blogApp.blogpost.service.interfaces.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CacheService cacheService;
    private final SlugUtils slugUtils;

    @Value("${app.service.name:post}")
    private String serviceName;

    @Value("${blog.post.cache.prefix:blog}")
    private String cachePrefix;

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private Long cacheTtl;

    private static final String CACHE_TYPE = "categories";

    private String getCacheKey(String key) {
        return String.format("%s:%s:%s:%s", cachePrefix, serviceName, CACHE_TYPE, key);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Bắt đầu tạo danh mục mới: {}", categoryDTO.getName());

        // Tạo request từ DTO
        CategoryDTO request = CategoryDTO.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .parentId(categoryDTO.getParentId())
                .build();

        // Tạo slug từ tên
        String slug = slugUtils.createSlug(request.getName());
        int counter = 1;
        while (categoryRepository.existsBySlug(slug)) {
            slug = slugUtils.createSlug(request.getName()) + "-" + counter++;
        }

        // Tạo category mới
        Category category = categoryMapper.toEntity(request);
        category.setSlug(slug);

        // Nếu có parent, kiểm tra tồn tại
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Đã tạo thành công danh mục với id {}", savedCategory.getId());

        // Xóa cache liên quan
        cacheService.delete(CACHE_TYPE, "all");
        cacheService.delete(CACHE_TYPE, "roots");
        if (request.getParentId() != null) {
            cacheService.delete(CACHE_TYPE, "parent:" + request.getParentId());
        }

        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryDTO getCategoryById(UUID id) {
        log.debug("Lấy thông tin danh mục {}", id);

        // Thử lấy từ cache trước
        String cacheKey = "id:" + id;
        Object cachedCategory = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedCategory != null) {
            log.debug("Lấy danh mục {} từ cache", id);
            return (CategoryDTO) cachedCategory;
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy danh mục với id {}", id);
                    return new CategoryNotFoundException(id);
                });

        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, categoryDTO, cacheTtl / 1000, TimeUnit.SECONDS);

        return categoryDTO;
    }

    @Override
    public CategoryDTO getCategoryBySlug(String slug) {
        log.debug("Lấy thông tin danh mục theo slug: {}", slug);

        // Thử lấy từ cache trước
        String cacheKey = "slug:" + slug;
        Object cachedCategory = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedCategory != null) {
            log.debug("Lấy danh mục với slug {} từ cache", slug);
            return (CategoryDTO) cachedCategory;
        }

        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy danh mục với slug {}", slug);
                    return new CategoryNotFoundException(slug);
                });

        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, categoryDTO, cacheTtl / 1000, TimeUnit.SECONDS);

        return categoryDTO;
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(UUID id, CategoryDTO categoryDTO) {
        log.info("Bắt đầu cập nhật danh mục {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy danh mục với id {}", id);
                    return new CategoryNotFoundException(id);
                });

        // Tạo request từ DTO
        CategoryDTO request = CategoryDTO.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .parentId(categoryDTO.getParentId())
                .build();

        // Cập nhật thông tin cơ bản
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        // Cập nhật slug nếu tên thay đổi
        if (!category.getName().equals(request.getName())) {
            String newSlug = slugUtils.createSlug(request.getName());
            int counter = 1;
            while (categoryRepository.existsBySlug(newSlug)) {
                newSlug = slugUtils.createSlug(request.getName()) + "-" + counter++;
            }
            category.setSlug(newSlug);
        }

        // Cập nhật parent nếu có
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Đã cập nhật thành công danh mục {}", id);

        // Xóa cache liên quan
        cacheService.delete(CACHE_TYPE, "id:" + id);
        cacheService.delete(CACHE_TYPE, "slug:" + category.getSlug());
        cacheService.delete(CACHE_TYPE, "all");
        cacheService.delete(CACHE_TYPE, "roots");
        if (category.getParent() != null) {
            cacheService.delete(CACHE_TYPE, "parent:" + category.getParent().getId());
        }

        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Bắt đầu xóa danh mục {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy danh mục với id {}", id);
                    return new CategoryNotFoundException(id);
                });

        // Xóa cache trước khi xóa
        cacheService.delete(CACHE_TYPE, "id:" + id);
        cacheService.delete(CACHE_TYPE, "slug:" + category.getSlug());
        cacheService.delete(CACHE_TYPE, "all");
        cacheService.delete(CACHE_TYPE, "roots");
        if (category.getParent() != null) {
            cacheService.delete(CACHE_TYPE, "parent:" + category.getParent().getId());
        }

        categoryRepository.delete(category);
        log.info("Đã xóa thành công danh mục {}", id);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        log.debug("Lấy tất cả danh mục");

        // Thử lấy từ cache trước
        Object cachedCategories = cacheService.get(CACHE_TYPE, "all");
        if (cachedCategories != null) {
            log.debug("Lấy danh sách danh mục từ cache");
            return (List<CategoryDTO>) cachedCategories;
        }

        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, "all", categoryDTOs, cacheTtl / 1000, TimeUnit.SECONDS);

        return categoryDTOs;
    }

    @Override
    public List<CategoryDTO> getRootCategories() {
        log.debug("Lấy danh sách danh mục gốc");

        // Thử lấy từ cache trước
        Object cachedRoots = cacheService.get(CACHE_TYPE, "roots");
        if (cachedRoots != null) {
            log.debug("Lấy danh sách danh mục gốc từ cache");
            return (List<CategoryDTO>) cachedRoots;
        }

        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        List<CategoryDTO> categoryDTOs = rootCategories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, "roots", categoryDTOs, cacheTtl / 1000, TimeUnit.SECONDS);

        return categoryDTOs;
    }

    @Override
    public List<CategoryDTO> getSubCategories(UUID parentId) {
        log.debug("Lấy danh sách danh mục con của danh mục {}", parentId);

        // Thử lấy từ cache trước
        String cacheKey = "parent:" + parentId;
        Object cachedSubs = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedSubs != null) {
            log.debug("Lấy danh sách danh mục con từ cache");
            return (List<CategoryDTO>) cachedSubs;
        }

        List<Category> subCategories = categoryRepository.findByParentId(parentId);
        List<CategoryDTO> categoryDTOs = subCategories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, categoryDTOs, cacheTtl / 1000, TimeUnit.SECONDS);

        return categoryDTOs;
    }

    @Override
    public PagedResponse<CategoryDTO> getPopularCategories(int pageNo, int pageSize) {
        log.debug("Lấy danh sách danh mục phổ biến - trang {}, kích thước {}", pageNo, pageSize);

        // Thử lấy từ cache trước
        String cacheKey = "popular:page:" + pageNo + ":size:" + pageSize;
        Object cachedResponse = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách danh mục phổ biến từ cache");
            return (PagedResponse<CategoryDTO>) cachedResponse;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Category> categoryPage = categoryRepository.findPopularCategories(pageable);

        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());

        PagedResponse<CategoryDTO> response = PagedResponse.<CategoryDTO>builder()
                .content(categoryDTOs)
                .pageNo(categoryPage.getNumber())
                .size(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .last(categoryPage.isLast())
                .build();

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);

        return response;
    }

    @Override
    public long countPostsInCategory(UUID categoryId) {
        log.debug("Đếm số bài viết trong danh mục {}", categoryId);

        // Thử lấy từ cache trước
        String cacheKey = "count:" + categoryId;
        Object cachedCount = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedCount != null) {
            log.debug("Lấy số lượng bài viết từ cache");
            return (long) cachedCount;
        }

        long count = categoryRepository.countPublishedPosts(categoryId);

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, count, cacheTtl / 1000, TimeUnit.SECONDS);

        return count;
    }
}