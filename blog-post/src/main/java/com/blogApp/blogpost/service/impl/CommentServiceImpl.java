package com.blogApp.blogpost.service.impl;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogpost.client.AuthServiceClient;
import com.blogApp.blogpost.dto.request.CommentCreateRequest;
import com.blogApp.blogpost.dto.response.CommentDTO;
import com.blogApp.blogpost.exception.CommentNotFoundException;
import com.blogApp.blogpost.mapper.CommentMapper;
import com.blogApp.blogpost.model.Comment;
import com.blogApp.blogpost.model.CommentStatus;
import com.blogApp.blogpost.model.Post;
import com.blogApp.blogpost.repository.CommentRepository;
import com.blogApp.blogpost.repository.PostRepository;
import com.blogApp.blogpost.service.interfaces.CommentService;
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

/**
 * Service class triển khai các chức năng quản lý bình luận
 * - Tạo, cập nhật, xóa bình luận
 * - Tìm kiếm bình luận theo nhiều tiêu chí
 * - Phân trang kết quả
 * - Quản lý cache
 * - Xử lý cấu trúc phân cấp (parent-child)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final AuthServiceClient authServiceClient;
    private final CacheService cacheService;

    @Value("${app.service.name:post}")
    private String serviceName;

    @Value("${blog.post.cache.prefix:blog}")
    private String cachePrefix;

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private Long cacheTtl;

    private static final String CACHE_TYPE = "comments";

    private String getCacheKey(String key) {
        return String.format("%s:%s:%s:%s", cachePrefix, serviceName, CACHE_TYPE, key);
    }

    @Override
    @Transactional
    public CommentDTO createComment(UUID postId, CommentCreateRequest request, String userId) {
        log.info("Bắt đầu tạo bình luận mới cho bài viết {} bởi user {}", postId, userId);

        // Kiểm tra bài viết tồn tại và cho phép bình luận
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bài viết với id {}", postId);
                    return new CommentNotFoundException(postId);
                });

        if (!post.isCommentEnabled()) {
            log.error("Bài viết {} không cho phép bình luận", postId);
            throw new IllegalStateException("Bài viết không cho phép bình luận");
        }

        // Lấy thông tin user
        var userInfo = authServiceClient.getUserInfo(userId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy thông tin user với id {}", userId);
                    return new IllegalStateException("Không tìm thấy thông tin user");
                });

        // Tạo comment mới
        Comment comment = commentMapper.toEntity(request);
        comment.setPost(post);
        comment.setAuthorId(Long.toString(userInfo.getId()));
        comment.setAuthorName(userInfo.getUsername());
        comment.setStatus(CommentStatus.PENDING);

        // Nếu là reply, kiểm tra comment cha
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy bình luận cha với id {}", request.getParentId());
                        return new CommentNotFoundException(request.getParentId());
                    });
            comment.setParent(parent);
        }

        Comment savedComment = commentRepository.save(comment);
        log.info("Đã tạo thành công bình luận với id {}", savedComment.getId());

        // Xóa cache liên quan
        cacheService.delete(CACHE_TYPE, "post:" + postId);
        cacheService.delete(CACHE_TYPE, "author:" + userId);
        cacheService.delete(CACHE_TYPE, "status:" + CommentStatus.PENDING);

        return commentMapper.toDto(savedComment);
    }

    @Override
    public CommentDTO getCommentById(UUID id) {
        log.debug("Lấy thông tin bình luận {}", id);
        
        // Thử lấy từ cache trước
        String cacheKey = "id:" + id;
        Object cachedComment = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedComment != null) {
            log.debug("Lấy bình luận {} từ cache", id);
            return (CommentDTO) cachedComment;
        }
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bình luận với id {}", id);
                    return new CommentNotFoundException(id);
                });

        CommentDTO commentDTO = commentMapper.toDto(comment);
        
        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, commentDTO, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return commentDTO;
    }

    @Override
    @Transactional
    public CommentDTO updateComment(UUID id, String content) {
        log.info("Bắt đầu cập nhật nội dung bình luận {}", id);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bình luận với id {}", id);
                    return new CommentNotFoundException(id);
                });

        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);
        log.info("Đã cập nhật thành công bình luận {}", id);

        // Xóa cache liên quan
        cacheService.delete(CACHE_TYPE, "id:" + id);
        cacheService.delete(CACHE_TYPE, "post:" + comment.getPost().getId());
        cacheService.delete(CACHE_TYPE, "author:" + comment.getAuthorId());

        return commentMapper.toDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID id) {
        log.info("Bắt đầu xóa bình luận {}", id);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bình luận với id {}", id);
                    return new CommentNotFoundException(id);
                });

        // Xóa cache trước khi xóa comment
        cacheService.delete(CACHE_TYPE, "id:" + id);
        cacheService.delete(CACHE_TYPE, "post:" + comment.getPost().getId());
        cacheService.delete(CACHE_TYPE, "author:" + comment.getAuthorId());
        cacheService.delete(CACHE_TYPE, "status:" + comment.getStatus());

        commentRepository.delete(comment);
        log.info("Đã xóa thành công bình luận {}", id);
    }

    @Override
    public PagedResponse<CommentDTO> getCommentsByPost(UUID postId, int pageNo, int pageSize) {
        log.debug("Lấy danh sách bình luận của bài viết {} - trang {}, kích thước {}", 
                 postId, pageNo, pageSize);

        // Thử lấy từ cache trước
        String cacheKey = "post:" + postId + ":page:" + pageNo + ":size:" + pageSize;
        Object cachedResponse = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bình luận của bài viết {} từ cache", postId);
            return (PagedResponse<CommentDTO>) cachedResponse;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Comment> commentPage = commentRepository.findByPostIdAndStatusAndParentIsNull(
                postId, CommentStatus.APPROVED, pageable);

        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(comment -> {
                    CommentDTO dto = commentMapper.toDto(comment);
                    dto.setReplies(comment.getReplies().stream()
                            .filter(reply -> reply.getStatus() == CommentStatus.APPROVED)
                            .map(commentMapper::toDto)
                            .collect(Collectors.toSet()));
                    return dto;
                })
                .collect(Collectors.toList());

        PagedResponse<CommentDTO> response = PagedResponse.<CommentDTO>builder()
                .content(commentDTOs)
                .pageNo(commentPage.getNumber())
                .size(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .last(commentPage.isLast())
                .build();

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        log.debug("Tìm thấy {} bình luận", commentPage.getTotalElements());
        return response;
    }

    @Override
    public PagedResponse<CommentDTO> getCommentsByAuthor(String authorId, int pageNo, int pageSize) {
        log.debug("Lấy danh sách bình luận của tác giả {} - trang {}, kích thước {}", 
                 authorId, pageNo, pageSize);

        // Thử lấy từ cache trước
        String cacheKey = "author:" + authorId + ":page:" + pageNo + ":size:" + pageSize;
        Object cachedResponse = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bình luận của tác giả {} từ cache", authorId);
            return (PagedResponse<CommentDTO>) cachedResponse;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Comment> commentPage = commentRepository.findByAuthorIdAndStatus(authorId, CommentStatus.APPROVED, pageable);

        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());

        PagedResponse<CommentDTO> response = PagedResponse.<CommentDTO>builder()
                .content(commentDTOs)
                .pageNo(commentPage.getNumber())
                .size(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .last(commentPage.isLast())
                .build();

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        log.debug("Tìm thấy {} bình luận", commentPage.getTotalElements());
        return response;
    }

    @Override
    public PagedResponse<CommentDTO> getCommentsByStatus(CommentStatus status, int pageNo, int pageSize) {
        log.debug("Lấy danh sách bình luận theo trạng thái {} - trang {}, kích thước {}", 
                 status, pageNo, pageSize);

        // Thử lấy từ cache trước
        String cacheKey = "status:" + status + ":page:" + pageNo + ":size:" + pageSize;
        Object cachedResponse = cacheService.get(CACHE_TYPE, cacheKey);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bình luận theo trạng thái {} từ cache", status);
            return (PagedResponse<CommentDTO>) cachedResponse;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Comment> commentPage = commentRepository.findByStatus(status, pageable);

        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());

        PagedResponse<CommentDTO> response = PagedResponse.<CommentDTO>builder()
                .content(commentDTOs)
                .pageNo(commentPage.getNumber())
                .size(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .last(commentPage.isLast())
                .build();

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
        log.debug("Tìm thấy {} bình luận", commentPage.getTotalElements());
        return response;
    }

    @Override
    @Transactional
    public CommentDTO updateCommentStatus(UUID id, CommentStatus status) {
        log.info("Bắt đầu cập nhật trạng thái bình luận {} thành {}", id, status);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bình luận với id {}", id);
                    return new CommentNotFoundException(id);
                });

        comment.setStatus(status);
        Comment updatedComment = commentRepository.save(comment);
        log.info("Đã cập nhật thành công trạng thái bình luận {}", id);

        // Xóa cache liên quan
        cacheService.delete(CACHE_TYPE, "id:" + id);
        cacheService.delete(CACHE_TYPE, "post:" + comment.getPost().getId());
        cacheService.delete(CACHE_TYPE, "author:" + comment.getAuthorId());
        cacheService.delete(CACHE_TYPE, "status:" + status);

        return commentMapper.toDto(updatedComment);
    }
} 