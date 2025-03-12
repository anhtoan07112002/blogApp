package com.blogApp.blogpost.service.impl;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.dto.response.UserSummary;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogcommon.service.CacheService;
import com.blogApp.blogpost.client.AuthServiceClient;
import com.blogApp.blogpost.dto.request.CommentCreateRequest;
import com.blogApp.blogpost.dto.response.CommentDTO;
import com.blogApp.blogpost.exception.BlogPostServiceException;
import com.blogApp.blogpost.exception.UnauthorizedCommentActionException;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                    return new ResourceNotFoundException("Post", "id", postId.toString());
                });
                
        if (!post.isCommentEnabled()) {
            log.error("Bài viết {} không cho phép bình luận", postId);
            throw new BlogPostServiceException("Bài viết này không cho phép bình luận");
        }
        
        // Lấy thông tin user
        UserSummary userInfo = authServiceClient.getCurrentUser().getData();
        log.info("Đã lấy thông tin user {} từ auth service", userInfo.getUsername());
        
        // Tạo comment mới
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setAuthorId(userInfo.getId().toString());
        comment.setAuthorName(userInfo.getUsername());
        comment.setStatus(CommentStatus.PENDING);

        // Nếu là reply, kiểm tra comment cha
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> 
                        new ResourceNotFoundException("Comment", "id", request.getParentId().toString())
                    );
            comment.setParent(parent);
        }

        Comment savedComment = commentRepository.save(comment);
        log.info("Đã tạo thành công bình luận với id {}", savedComment.getId());

        // Xóa tất cả cache liên quan
        
        // 1. Xóa cache cho bài viết
        // Xóa cache của danh sách bình luận theo bài viết
        for (int i = 0; i < 5; i++) { // Giả sử xóa cache cho 5 trang đầu tiên
            String keyWithAllFalse = "post:" + postId + ":page:" + i + ":size:10:all:false";
            String keyWithAllTrue = "post:" + postId + ":page:" + i + ":size:10:all:true";
            cacheService.delete(CACHE_TYPE, keyWithAllFalse);
            cacheService.delete(CACHE_TYPE, keyWithAllTrue);
            log.debug("Đã xóa cache: {} và {}", keyWithAllFalse, keyWithAllTrue);
        }
        
        // 2. Xóa cache cho tác giả
        String authorId = userInfo.getId().toString();
        for (int i = 0; i < 3; i++) {
            cacheService.delete(CACHE_TYPE, "author:" + authorId + ":page:" + i + ":size:10");
            log.debug("Đã xóa cache bình luận của tác giả {}, trang {}", authorId, i);
        }
        
        // 3. Xóa cache cho trạng thái PENDING
        cacheService.delete(CACHE_TYPE, "status:" + CommentStatus.PENDING + ":page:0:size:10");
        log.debug("Đã xóa cache bình luận theo trạng thái {}", CommentStatus.PENDING);
        
        // 4. Nếu là reply, xóa cache của parent comment
        if (request.getParentId() != null) {
            cacheService.delete(CACHE_TYPE, "id:" + request.getParentId());
            log.debug("Đã xóa cache của bình luận cha {}", request.getParentId());
        }
        
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
                    return new ResourceNotFoundException("Comment", "id", id.toString());
                });

        CommentDTO commentDTO = commentMapper.toDto(comment);
        
        // Lưu vào cache
        cacheService.set(CACHE_TYPE, cacheKey, commentDTO, cacheTtl / 1000, TimeUnit.SECONDS);
        
        return commentDTO;
    }

    @Override
    @Transactional
    public CommentDTO updateComment(UUID id, String content, String userId) {
        log.info("Cập nhật bình luận với ID: {}", id);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        
        // Kiểm tra nếu người dùng là tác giả hoặc admin
        UserSummary currentUser = authServiceClient.getCurrentUser().getData();
        boolean isAdmin = currentUser.getRole().equals("ROLE_ADMIN");
        boolean isAuthor = comment.getAuthorId().equals(userId);
        
        if (!isAuthor && !isAdmin) {
            log.warn("User {} không có quyền cập nhật bình luận {}", userId, id);
            throw UnauthorizedCommentActionException.cannotUpdateComment();
        }
        
        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());
        
        // Xóa cache liên quan
        String cacheKey = "id:" + id;
        cacheService.delete(CACHE_TYPE, cacheKey);
        
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(UUID id, String userId) {
        log.info("Xóa bình luận với ID: {}", id);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        
        // Kiểm tra nếu người dùng là tác giả hoặc admin
        UserSummary currentUser = authServiceClient.getCurrentUser().getData();
        boolean isAdmin = currentUser.getRole().equals("ROLE_ADMIN");
        boolean isAuthor = comment.getAuthorId().equals(userId);
        
        if (!isAuthor && !isAdmin) {
            log.warn("User {} không có quyền xóa bình luận {}", userId, id);
            throw UnauthorizedCommentActionException.cannotDeleteComment();
        }
        
        // Xóa cache liên quan
        String cacheKey = "id:" + id;
        cacheService.delete(CACHE_TYPE, cacheKey);
        
        commentRepository.delete(comment);
    }

    @Override
    public PagedResponse<CommentDTO> getCommentsByPost(UUID postId, int pageNo, int pageSize) {
        return getCommentsByPost(postId, pageNo, pageSize, false);
    }
    
    /**
     * Lấy danh sách bình luận của bài viết, có thể bao gồm tất cả trạng thái
     * @param postId ID của bài viết
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param includeAllStatuses true để bao gồm tất cả trạng thái, false để chỉ lấy APPROVED
     * @return PagedResponse<CommentDTO> chứa danh sách bình luận
     */
    public PagedResponse<CommentDTO> getCommentsByPost(UUID postId, int pageNo, int pageSize, boolean includeAllStatuses) {
        log.debug("Lấy danh sách bình luận của bài viết {} - trang {}, kích thước {}, includeAllStatuses: {}", 
                 postId, pageNo, pageSize, includeAllStatuses);

        // Khóa cache tùy theo có lấy tất cả trạng thái hay không
        String key = "post:" + postId + ":page:" + pageNo + ":size:" + pageSize + ":all:" + includeAllStatuses;
        Object cachedResponse = cacheService.get(CACHE_TYPE, key);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bình luận của bài viết {} từ cache với key: {}", postId, key);
            return (PagedResponse<CommentDTO>) cachedResponse;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Comment> commentPage;
        
        if (includeAllStatuses) {
            // Lấy tất cả bình luận không phân biệt trạng thái
            commentPage = commentRepository.findByPostIdAndParentIsNull(postId, pageable);
            log.debug("Tìm tất cả bình luận cho bài viết {}", postId);
        } else {
            // Chỉ lấy bình luận đã được phê duyệt
            commentPage = commentRepository.findByPostIdAndStatusAndParentIsNull(
                    postId, CommentStatus.APPROVED, pageable);
            log.debug("Chỉ tìm bình luận đã APPROVED cho bài viết {}", postId);
        }

        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(comment -> {
                    CommentDTO dto = commentMapper.toDto(comment);
                    if (includeAllStatuses) {
                        // Bao gồm tất cả các replies không phân biệt trạng thái
                        dto.setReplies(comment.getReplies().stream()
                                .map(commentMapper::toDto)
                                .collect(Collectors.toSet()));
                    } else {
                        // Chỉ bao gồm replies đã được phê duyệt
                        dto.setReplies(comment.getReplies().stream()
                                .filter(reply -> reply.getStatus() == CommentStatus.APPROVED)
                                .map(commentMapper::toDto)
                                .collect(Collectors.toSet()));
                    }
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
                .first(commentPage.isFirst())
                .empty(commentPage.isEmpty())
                .build();

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, key, response, cacheTtl / 1000, TimeUnit.SECONDS);
        log.debug("Đã lưu kết quả vào cache với key: {}", key);
        
        log.debug("Tìm thấy {} bình luận", commentPage.getTotalElements());
        return response;
    }

    @Override
    public PagedResponse<CommentDTO> getCommentsByAuthor(String authorId, int pageNo, int pageSize) {
        return getCommentsByAuthor(authorId, pageNo, pageSize, false);
    }
    
    /**
     * Lấy danh sách bình luận của tác giả, có thể bao gồm tất cả trạng thái
     * @param authorId ID của tác giả
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param includeAllStatuses true để bao gồm tất cả trạng thái, false để chỉ lấy APPROVED
     * @return PagedResponse<CommentDTO> chứa danh sách bình luận
     */
    @Override
    public PagedResponse<CommentDTO> getCommentsByAuthor(String authorId, int pageNo, int pageSize, boolean includeAllStatuses) {
        log.debug("Lấy danh sách bình luận của tác giả {} - trang {}, kích thước {}, includeAllStatuses: {}", 
                 authorId, pageNo, pageSize, includeAllStatuses);

        // Khóa cache tùy theo có lấy tất cả trạng thái hay không
        String key = "author:" + authorId + ":page:" + pageNo + ":size:" + pageSize + ":all:" + includeAllStatuses;
        Object cachedResponse = cacheService.get(CACHE_TYPE, key);
        if (cachedResponse != null) {
            log.debug("Lấy danh sách bình luận của tác giả {} từ cache với key: {}", authorId, key);
            return (PagedResponse<CommentDTO>) cachedResponse;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Comment> commentPage;
        
        if (includeAllStatuses) {
            // Lấy tất cả bình luận không phân biệt trạng thái
            commentPage = commentRepository.findByAuthorId(authorId, pageable);
            log.debug("Tìm tất cả bình luận của tác giả {}", authorId);
        } else {
            // Chỉ lấy bình luận đã được phê duyệt
            commentPage = commentRepository.findByAuthorIdAndStatus(authorId, CommentStatus.APPROVED, pageable);
            log.debug("Chỉ tìm bình luận đã APPROVED của tác giả {}", authorId);
        }

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
                .first(commentPage.isFirst())
                .empty(commentPage.isEmpty())
                .build();

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, key, response, cacheTtl / 1000, TimeUnit.SECONDS);
        log.debug("Đã lưu kết quả vào cache với key: {}", key);
        
        log.debug("Tìm thấy {} bình luận của tác giả {}", commentPage.getTotalElements(), authorId);
        return response;
    }

    @Override
    public PagedResponse<CommentDTO> getCommentsByStatus(CommentStatus status, int pageNo, int pageSize) {
        log.debug("Lấy danh sách bình luận theo trạng thái {} - trang {}, kích thước {}", 
                 status, pageNo, pageSize);

        // Thử lấy từ cache trước
        String key = "status:" + status + ":page:" + pageNo + ":size:" + pageSize;
        Object cachedResponse = cacheService.get(CACHE_TYPE, key);
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
                .first(commentPage.isFirst())
                .empty(commentPage.isEmpty())
                .build();

        // Lưu vào cache
        cacheService.set(CACHE_TYPE, key, response, cacheTtl / 1000, TimeUnit.SECONDS);
        
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
                    return new ResourceNotFoundException("Comment", "id", id.toString());
                });

        CommentStatus oldStatus = comment.getStatus(); // Lưu lại trạng thái cũ để xóa cache
        comment.setStatus(status);
        Comment updatedComment = commentRepository.save(comment);
        log.info("Đã cập nhật thành công trạng thái bình luận {}", id);

        // Xóa cache liên quan đến bình luận này
        cacheService.delete(CACHE_TYPE, "id:" + id);
        
        // Xóa cache liên quan đến bài viết
        UUID postId = comment.getPost().getId();
        
        // Xóa cache của danh sách bình luận theo bài viết
        for (int i = 0; i < 5; i++) { // Giả sử xóa cache cho 5 trang đầu tiên
            String keyWithAllFalse = "post:" + postId + ":page:" + i + ":size:10:all:false";
            String keyWithAllTrue = "post:" + postId + ":page:" + i + ":size:10:all:true";
            cacheService.delete(CACHE_TYPE, keyWithAllFalse);
            cacheService.delete(CACHE_TYPE, keyWithAllTrue);
            log.debug("Đã xóa cache: {} và {}", keyWithAllFalse, keyWithAllTrue);
        }
        
        // Xóa cache liên quan đến tác giả
        String authorId = comment.getAuthorId();
        for (int i = 0; i < 3; i++) {
            cacheService.delete(CACHE_TYPE, "author:" + authorId + ":page:" + i + ":size:10");
            log.debug("Đã xóa cache bình luận của tác giả {}, trang {}", authorId, i);
        }
        
        // Xóa cache liên quan đến trạng thái
        cacheService.delete(CACHE_TYPE, "status:" + oldStatus + ":page:0:size:10");
        cacheService.delete(CACHE_TYPE, "status:" + status + ":page:0:size:10");
        log.debug("Đã xóa cache bình luận theo trạng thái {} và {}", oldStatus, status);

        return commentMapper.toDto(updatedComment);
    }
} 