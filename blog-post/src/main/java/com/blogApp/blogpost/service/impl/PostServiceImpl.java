package com.blogApp.blogpost.service.impl;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogpost.client.AuthServiceClient;
import com.blogApp.blogpost.dto.request.PostCreateRequest;
import com.blogApp.blogpost.dto.request.PostUpdateRequest;
import com.blogApp.blogpost.dto.response.PostSummaryDTO;
import com.blogApp.blogpost.dto.CategoryDTO;
import com.blogApp.blogpost.model.Category;
import com.blogApp.blogpost.model.CommentStatus;
import com.blogApp.blogpost.model.Post;
import com.blogApp.blogcommon.enums.PostStatus;
import com.blogApp.blogpost.model.Tag;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogpost.mapper.PostMapper;
import com.blogApp.blogpost.repository.CategoryRepository;
import com.blogApp.blogpost.repository.CommentRepository;
import com.blogApp.blogpost.repository.PostRepository;
import com.blogApp.blogpost.repository.TagRepository;
import com.blogApp.blogpost.service.interfaces.PostService;
import com.blogApp.blogpost.util.SlugUtils;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.blogApp.blogcommon.dto.response.UserProfile;

/**
 * Service class triển khai các chức năng quản lý bài viết, bao gồm:
 * - Tạo, cập nhật, xóa bài viết
 * - Tìm kiếm bài viết theo nhiều tiêu chí khác nhau
 * - Phân trang kết quả
 * - Quản lý cache
 * - Xử lý markdown sang HTML
 * - Tích hợp với các service khác như auth service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;
    private final AuthServiceClient authServiceClient;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;
    private final SlugUtils slugUtils;
    private final CacheService cacheService;

    @Value("${app.service.name:post}")
    private String serviceName;

    @Value("${blog.post.cache.prefix:blog}")
    private String cachePrefix;

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private Long cacheTtl;

    private static final String POST_CACHE_TYPE = "posts";
    private static final String POST_LIST_CACHE_TYPE = "postLists";

    /**
     * Tạo bài viết mới với thông tin từ request và user id
     * - Lấy thông tin user từ auth service
     * - Tạo slug duy nhất từ tiêu đề
     * - Thêm categories và tags
     * - Lưu bài viết và trả về DTO
     */
    @Override
    @Transactional
    public PostSummaryDTO createPost(PostCreateRequest createPostRequest, String userId) {
        log.info("Bắt đầu tạo bài viết mới cho user {}", userId);
        
        UserProfile userInfo = authServiceClient.getUserInfo(userId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy thông tin user với id {}", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        Post post = new Post();
        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        post.setSummary(createPostRequest.getSummary());
        post.setStatus(createPostRequest.getStatus());
        post.setCommentEnabled(createPostRequest.isCommentEnabled());
        post.setAuthorId(Long.toString(userInfo.getId()));
        post.setAuthorName(userInfo.getUsername());
        post.setViewCount(0);

        // Generate slug if not provided
        String slug = createPostRequest.getTitle();
        if (slug == null || slug.isEmpty()) {
            slug = slugUtils.createSlug(createPostRequest.getTitle());
            log.debug("Đã tạo slug '{}' từ tiêu đề '{}'", slug, createPostRequest.getTitle());
        }

        // Ensure slug is unique
        String finalSlug = slug;
        int counter = 1;
        while (postRepository.existsBySlug(finalSlug)) {
            finalSlug = slug + "-" + counter++;
            log.debug("Slug '{}' đã tồn tại, thử với slug mới '{}'", slug, finalSlug);
        }
        post.setSlug(finalSlug);

        // Set publish date if status is PUBLISHED
        if (post.getStatus() == PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }

        // Add categories
        if (createPostRequest.getCategoryIds() != null && !createPostRequest.getCategoryIds().isEmpty()) {
            log.debug("Thêm {} danh mục cho bài viết", createPostRequest.getCategoryIds().size());
            for (UUID categoryId : createPostRequest.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> {
                            log.error("Không tìm thấy danh mục với id {}", categoryId);
                            return new ResourceNotFoundException("Category", "id", categoryId.toString());
                        });
                post.addCategory(category);
            }
        }

        // Add tags
        if (createPostRequest.getTags() != null && !createPostRequest.getTags().isEmpty()) {
            log.debug("Thêm {} tag cho bài viết", createPostRequest.getTags().size());
            for (String tagName : createPostRequest.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            log.debug("Tạo tag mới '{}'", tagName);
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setSlug(slugUtils.createSlug(tagName));
                            return tagRepository.save(newTag);
                        });
                post.addTag(tag);
            }
        }

        Post savedPost = postRepository.save(post);
        log.info("Đã tạo thành công bài viết với id {}", savedPost.getId());

        // Xóa cache liên quan
        cacheService.delete(POST_CACHE_TYPE, "id:" + savedPost.getId());
        cacheService.delete(POST_CACHE_TYPE, "slug:" + savedPost.getSlug());
        cacheService.delete(POST_LIST_CACHE_TYPE, "author:" + userId);
        cacheService.delete(POST_LIST_CACHE_TYPE, "status:" + savedPost.getStatus());

        PostSummaryDTO postDTO = convertToPostSummaryDTO(postMapper.toSummaryDto(savedPost));
        postDTO.setCommentCount(0L);
        return postDTO;
    }

    /**
     * Lấy thông tin bài viết theo id
     * - Kiểm tra tồn tại
     * - Chuyển đổi sang DTO
     * - Thêm số lượng comment đã duyệt
     */
    @Override
    public PostSummaryDTO getPostById(UUID id) {
        // Thử lấy từ cache trước
        String cacheKey = "id:" + id;
        Object cachedPost = cacheService.get(POST_CACHE_TYPE, cacheKey);
        if (cachedPost != null) {
            log.debug("Lấy bài viết {} từ cache", id);
            return (PostSummaryDTO) cachedPost;
        }

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id.toString()));

        PostSummaryDTO postDTO = convertToPostSummaryDTO(postMapper.toSummaryDto(post));
        postDTO.setCommentCount(commentRepository.countCommentsByPostIdAndStatus(id, CommentStatus.APPROVED));

        // Lưu vào cache
        cacheService.set(POST_CACHE_TYPE, cacheKey, postDTO, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return postDTO;
    }

    @Override
    public PostSummaryDTO getPostBySlug(String slug) {
        // Thử lấy từ cache trước
        String cacheKey = "slug:" + slug;
        Object cachedPost = cacheService.get(POST_CACHE_TYPE, cacheKey);
        if (cachedPost != null) {
            log.debug("Lấy bài viết với slug {} từ cache", slug);
            return (PostSummaryDTO) cachedPost;
        }

        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "slug", slug));

        PostSummaryDTO postDTO = convertToPostSummaryDTO(postMapper.toSummaryDto(post));
        postDTO.setCommentCount(commentRepository.countCommentsByPostIdAndStatus(post.getId(), CommentStatus.APPROVED));

        // Lưu vào cache
        cacheService.set(POST_CACHE_TYPE, cacheKey, postDTO, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return postDTO;
    }

    /**
     * Cập nhật thông tin bài viết
     * - Cập nhật các trường cơ bản
     * - Cập nhật slug nếu tiêu đề thay đổi
     * - Cập nhật trạng thái và thời gian xuất bản
     * - Cập nhật danh mục và tag
     */
    @Override
    @Transactional
    public PostSummaryDTO updatePost(UUID id, PostUpdateRequest updatePostRequest) {
        log.info("Bắt đầu cập nhật bài viết {}", id);
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bài viết với id {}", id);
                    return new ResourceNotFoundException("Post", "id", id.toString());
                });

        post.setTitle(updatePostRequest.getTitle());
        post.setContent(updatePostRequest.getContent());
        post.setSummary(updatePostRequest.getSummary());
        post.setCommentEnabled(updatePostRequest.isCommentEnabled());

        // Update slug if provided
        if (updatePostRequest.getTitle() != null && !updatePostRequest.getTitle().isEmpty()) {
            String newSlug = slugUtils.createSlug(updatePostRequest.getTitle());
            if (!post.getSlug().equals(newSlug) && !postRepository.existsBySlug(newSlug)) {
                post.setSlug(newSlug);
            }
        }

        // Update status
        if (post.getStatus() != updatePostRequest.getStatus()) {
            post.setStatus(updatePostRequest.getStatus());

            // Set publishedAt if status changes to PUBLISHED
            if (updatePostRequest.getStatus() == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
        }

        // Update categories
        if (updatePostRequest.getCategoryIds() != null) {
            // Clear existing categories
            new HashSet<>(post.getCategories()).forEach(post::removeCategory);

            // Add new categories
            updatePostRequest.getCategoryIds().forEach(categoryId -> {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId.toString()));
                post.addCategory(category);
            });
        }

        // Update tags
        if (updatePostRequest.getTags() != null) {
            // Clear existing tags
            new HashSet<>(post.getTags()).forEach(post::removeTag);

            // Add new tags
            updatePostRequest.getTags().forEach(tagName -> {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setSlug(slugUtils.createSlug(tagName));
                            return tagRepository.save(newTag);
                        });
                post.addTag(tag);
            });
        }

        Post updatedPost = postRepository.save(post);
        log.info("Đã cập nhật thành công bài viết {}", id);

        // Xóa cache liên quan
        cacheService.delete(POST_CACHE_TYPE, "id:" + id);
        cacheService.delete(POST_CACHE_TYPE, "slug:" + post.getSlug());
        cacheService.delete(POST_LIST_CACHE_TYPE, "author:" + post.getAuthorId());
        cacheService.delete(POST_LIST_CACHE_TYPE, "status:" + post.getStatus());

        PostSummaryDTO postDTO = convertToPostSummaryDTO(postMapper.toSummaryDto(updatedPost));
        postDTO.setCommentCount(commentRepository.countCommentsByPostIdAndStatus(id, CommentStatus.APPROVED));
        return postDTO;
    }

    @Override
    @Transactional
    public void deletePost(UUID id) {
        log.info("Bắt đầu xóa bài viết {}", id);
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bài viết với id {}", id);
                    return new ResourceNotFoundException("Post", "id", id.toString());
                });

        // Xóa cache trước khi xóa bài viết
        cacheService.delete(POST_CACHE_TYPE, "id:" + id);
        cacheService.delete(POST_CACHE_TYPE, "slug:" + post.getSlug());
        cacheService.delete(POST_LIST_CACHE_TYPE, "author:" + post.getAuthorId());
        cacheService.delete(POST_LIST_CACHE_TYPE, "status:" + post.getStatus());
                
        postRepository.delete(post);
        log.info("Đã xóa thành công bài viết {}", id);
    }

    @Override
    public PagedResponse<PostSummaryDTO> getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        log.debug("Lấy danh sách bài viết - trang {}, kích thước {}, sắp xếp theo {} {}", 
                 pageNo, pageSize, sortBy, sortDir);

        // Thử lấy từ cache trước
        String cacheKey = "all:page:" + pageNo + ":size:" + pageSize + ":sort:" + sortBy + ":" + sortDir;
        Object cachedResponse = cacheService.get(POST_LIST_CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bài viết từ cache");
            return (PagedResponse<PostSummaryDTO>) cachedResponse;
        }
                 
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> postPage = postRepository.findAll(pageable);
        
        log.debug("Tìm thấy {} bài viết", postPage.getTotalElements());
        
        PagedResponse<PostSummaryDTO> response = createPostPageResponse(postPage);
        
        // Lưu vào cache
        cacheService.set(POST_LIST_CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return response;
    }

    @Override
    public PagedResponse<PostSummaryDTO> getPostsByStatus(PostStatus status, int pageNo, int pageSize, String sortBy, String sortDir) {
        // Thử lấy từ cache trước
        String cacheKey = "status:" + status + ":page:" + pageNo + ":size:" + pageSize + ":sort:" + sortBy + ":" + sortDir;
        Object cachedResponse = cacheService.get(POST_LIST_CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bài viết theo trạng thái {} từ cache", status);
            return (PagedResponse<PostSummaryDTO>) cachedResponse;
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> postPage = postRepository.findByStatus(status, pageable);

        PagedResponse<PostSummaryDTO> response = createPostPageResponse(postPage);
        
        // Lưu vào cache
        cacheService.set(POST_LIST_CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return response;
    }

    @Override
    public PagedResponse<PostSummaryDTO> getPostsByAuthor(String authorId, int pageNo, int pageSize, String sortBy, String sortDir) {
        // Thử lấy từ cache trước
        String cacheKey = "author:" + authorId + ":page:" + pageNo + ":size:" + pageSize + ":sort:" + sortBy + ":" + sortDir;
        Object cachedResponse = cacheService.get(POST_LIST_CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bài viết của tác giả {} từ cache", authorId);
            return (PagedResponse<PostSummaryDTO>) cachedResponse;
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> postPage = postRepository.findByAuthorId(authorId, pageable);

        PagedResponse<PostSummaryDTO> response = createPostPageResponse(postPage);
        
        // Lưu vào cache
        cacheService.set(POST_LIST_CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return response;
    }

    @Override
    public PagedResponse<PostSummaryDTO> getPostsByCategory(UUID categoryId, int pageNo, int pageSize, String sortBy, String sortDir) {
        // Thử lấy từ cache trước
        String cacheKey = "category:" + categoryId + ":page:" + pageNo + ":size:" + pageSize + ":sort:" + sortBy + ":" + sortDir;
        Object cachedResponse = cacheService.get(POST_LIST_CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bài viết theo danh mục {} từ cache", categoryId);
            return (PagedResponse<PostSummaryDTO>) cachedResponse;
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> postPage = postRepository.findByCategoryIdAndStatus(categoryId, PostStatus.PUBLISHED, pageable);

        PagedResponse<PostSummaryDTO> response = createPostPageResponse(postPage);
        
        // Lưu vào cache
        cacheService.set(POST_LIST_CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return response;
    }

    @Override
    public PagedResponse<PostSummaryDTO> getPostsByTag(UUID tagId, int pageNo, int pageSize, String sortBy, String sortDir) {
        // Thử lấy từ cache trước
        String cacheKey = "tag:" + tagId + ":page:" + pageNo + ":size:" + pageSize + ":sort:" + sortBy + ":" + sortDir;
        Object cachedResponse = cacheService.get(POST_LIST_CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bài viết theo tag {} từ cache", tagId);
            return (PagedResponse<PostSummaryDTO>) cachedResponse;
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> postPage = postRepository.findByTagIdAndStatus(tagId, PostStatus.PUBLISHED, pageable);

        PagedResponse<PostSummaryDTO> response = createPostPageResponse(postPage);
        
        // Lưu vào cache
        cacheService.set(POST_LIST_CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return response;
    }

    @Override
    public PagedResponse<PostSummaryDTO> searchPosts(String keyword, int pageNo, int pageSize) {
        // Thử lấy từ cache trước
        String cacheKey = "search:" + keyword + ":page:" + pageNo + ":size:" + pageSize;
        Object cachedResponse = cacheService.get(POST_LIST_CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy kết quả tìm kiếm bài viết với từ khóa '{}' từ cache", keyword);
            return (PagedResponse<PostSummaryDTO>) cachedResponse;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Post> postPage = postRepository.searchByKeyword(keyword, pageable);

        PagedResponse<PostSummaryDTO> response = createPostPageResponse(postPage);
        
        // Lưu vào cache
        cacheService.set(POST_LIST_CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return response;
    }

    @Override
    @Transactional
    public PostSummaryDTO updatePostStatus(UUID id, PostStatus status) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id.toString()));

        post.setStatus(status);

        // Set publishedAt if status changes to PUBLISHED
        if (status == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }

        Post updatedPost = postRepository.save(post);

        // Xóa cache liên quan
        cacheService.delete(POST_CACHE_TYPE, "id:" + id);
        cacheService.delete(POST_CACHE_TYPE, "slug:" + post.getSlug());
        cacheService.delete(POST_LIST_CACHE_TYPE, "author:" + post.getAuthorId());
        cacheService.delete(POST_LIST_CACHE_TYPE, "status:" + status);

        PostSummaryDTO postDTO = convertToPostSummaryDTO(postMapper.toSummaryDto(updatedPost));
        postDTO.setCommentCount(commentRepository.countCommentsByPostIdAndStatus(id, CommentStatus.APPROVED));
        return postDTO;
    }

    @Override
    @Transactional
    public PostSummaryDTO incrementViewCount(UUID id) {
        log.debug("Tăng lượt xem cho bài viết {}", id);
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bài viết với id {}", id);
                    return new ResourceNotFoundException("Post", "id", id.toString());
                });

        post.setViewCount(post.getViewCount() + 1);
        Post updatedPost = postRepository.save(post);
        log.debug("Đã tăng lượt xem bài viết {} lên {}", id, updatedPost.getViewCount());

        // Xóa cache liên quan
        cacheService.delete(POST_CACHE_TYPE, "id:" + id);
        cacheService.delete(POST_CACHE_TYPE, "slug:" + post.getSlug());

        PostSummaryDTO postDTO = convertToPostSummaryDTO(postMapper.toSummaryDto(updatedPost));
        postDTO.setCommentCount(commentRepository.countCommentsByPostIdAndStatus(id, CommentStatus.APPROVED));
        return postDTO;
    }

    /**
     * Tạo đối tượng PagedResponse từ Page<Post>
     * - Chuyển đổi posts sang DTOs
     * - Thêm số lượng comment cho mỗi bài viết
     * - Chuyển đổi markdown sang HTML nếu cần
     * - Đóng gói kết quả với thông tin phân trang
     */
    private PagedResponse<PostSummaryDTO> createPostPageResponse(Page<Post> postPage) {
        log.debug("Bắt đầu chuyển đổi {} bài viết sang DTO", postPage.getNumberOfElements());
        
        List<Post> posts = postPage.getContent();
        List<PostSummaryDTO> postDTOs = posts.stream().map(post -> {
            PostSummaryDTO dto = convertToPostSummaryDTO(postMapper.toSummaryDto(post));
            dto.setCommentCount(commentRepository.countCommentsByPostIdAndStatus(post.getId(), CommentStatus.APPROVED));

            // Convert markdown to HTML if needed
            if (post.getContent() != null) {
                log.trace("Chuyển đổi markdown sang HTML cho bài viết {}", post.getId());
                Node document = markdownParser.parse(post.getContent());
                String html = htmlRenderer.render(document);
            }

            return dto;
        }).collect(Collectors.toList());

        log.debug("Đã chuyển đổi thành công {} bài viết sang DTO", postDTOs.size());
        
        return PagedResponse.<PostSummaryDTO>builder()
                .content(postDTOs)
                .pageNo(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .last(postPage.isLast())
                .build();
    }

    /**
     * Chuyển đổi từ PostSummaryDto sang PostSummaryDTO
     * - Copy các trường cơ bản
     * - Chuyển đổi categories sang CategoryDTO
     * - Giữ nguyên tags
     */
    private PostSummaryDTO convertToPostSummaryDTO(com.blogApp.blogcommon.dto.PostSummaryDTO dto) {
        return PostSummaryDTO.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .slug(dto.getSlug())
                .summary(dto.getSummary())
                .authorId(dto.getAuthorId())
                .authorName(dto.getAuthorName())
                .status(dto.getStatus())
                .viewCount(dto.getViewCount())
                .publishedAt(dto.getPublishedAt())
                .createdAt(dto.getCreatedAt())
                .categories(dto.getCategories().stream()
                        .map(category -> CategoryDTO.builder()
                                .id(category.getId())
                                .name(category.getName())
                                .slug(category.getSlug())
                                .build())
                        .collect(Collectors.toSet()))
                .tags(dto.getTags())
                .build();
    }
}
