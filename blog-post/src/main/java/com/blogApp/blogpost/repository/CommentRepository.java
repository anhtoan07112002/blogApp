package com.blogApp.blogpost.repository;

import com.blogApp.blogpost.model.Comment;
import com.blogApp.blogpost.model.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository cho Comment
 * - Tạo các phương thức để thực hiện các thao tác với Comment 
 * - Sử dụng JPA Repository để tương tác với database
 * - Tìm kiếm bình luận theo postId và status @Page<Comment> findByPostIdAndStatusAndParentIsNull(UUID postId, CommentStatus status, Pageable pageable);
 * - Tìm kiếm bình luận theo parentId và status @List<Comment> findByParentIdAndStatus(UUID parentId, CommentStatus status);
 * - Tìm kiếm bình luận theo authorId và status @Page<Comment> findByAuthorIdAndStatus(String authorId, CommentStatus status, Pageable pageable);
 * - Tìm kiếm bình luận theo status @Page<Comment> findByStatus(CommentStatus status, Pageable pageable);
 * - Tìm kiếm bình luận mới nhất theo postId và status @Page<Comment> findLatestCommentsByPostId(@Param("postId") UUID postId, @Param("status") CommentStatus status, Pageable pageable);
 * - Đếm số bình luận theo postId và status @long countCommentsByPostIdAndStatus(@Param("postId") UUID postId, @Param("status") CommentStatus status);
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByPostIdAndStatusAndParentIsNull(UUID postId, CommentStatus status, Pageable pageable);

    Page<Comment> findByPostIdAndParentIsNull(UUID postId, Pageable pageable);

    List<Comment> findByParentIdAndStatus(UUID parentId, CommentStatus status);

    Page<Comment> findByAuthorIdAndStatus(String authorId, CommentStatus status, Pageable pageable);

    Page<Comment> findByAuthorId(String authorId, Pageable pageable);

    Page<Comment> findByStatus(CommentStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findLatestCommentsByPostId(@Param("postId") UUID postId, @Param("status") CommentStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.status = :status")
    long countCommentsByPostIdAndStatus(@Param("postId") UUID postId, @Param("status") CommentStatus status);
}