package com.blogApp.blogpost.mapper;

import com.blogApp.blogcommon.dto.CommentSummaryDTO;
import com.blogApp.blogpost.dto.request.CommentCreateRequest;
import com.blogApp.blogpost.dto.response.CommentDTO;
import com.blogApp.blogpost.model.Comment;
import com.blogApp.blogpost.model.CommentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interface mapper để chuyển đổi giữa các đối tượng liên quan đến Comment
 * - Chuyển đổi giữa entity và các DTO
 * - Hỗ trợ mapping cho cấu trúc phân cấp (parent-child)
 * - Tự động set các giá trị mặc định khi tạo mới
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    /**
     * Chuyển từ request sang entity khi tạo mới
     * Set các giá trị mặc định và bỏ qua các trường tự sinh
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentCreateRequest request);

    /**
     * Chuyển sang DTO đầy đủ
     * Map các quan hệ phức tạp như post, parent và replies
     */
    @Mapping(target = "postId", expression = "java(comment.getPost().getId())")
    @Mapping(target = "parentId", expression = "java(comment.getParent() != null ? comment.getParent().getId() : null)")
    @Mapping(target = "replies", expression = "java(mapRepliesToDto(comment.getReplies()))")
    CommentDTO toDto(Comment comment);

    /**
     * Chuyển sang DTO tóm tắt
     * Map postId thành postTitle và postSlug
     */
    @Mapping(target = "postId", expression = "java(comment.getPost().getId())")
    @Mapping(target = "postTitle", expression = "java(comment.getPost().getTitle())")
    @Mapping(target = "postSlug", expression = "java(comment.getPost().getSlug())")
    CommentSummaryDTO toSummaryDto(Comment comment);

    /**
     * Helper method để map danh sách replies sang DTO
     * Chỉ lấy các replies đã được phê duyệt
     */
    default Set<CommentDTO> mapRepliesToDto(Set<Comment> replies) {
        if (replies == null) {
            return Set.of();
        }
        return replies.stream()
                .filter(reply -> reply.getStatus() == CommentStatus.APPROVED)
                .map(reply -> new CommentDTO(
                        reply.getId(),
                        reply.getContent(),
                        reply.getAuthorId(),
                        reply.getAuthorName(),
                        reply.getStatus(),
                        reply.getPost().getId(),
                        reply.getParent().getId(),
                        reply.getCreatedAt(),
                        reply.getUpdatedAt(),
                        Set.of() // Tránh đệ quy sâu
                ))
                .collect(Collectors.toSet());
    }
}
