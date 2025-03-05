package com.blogApp.blogpost.repository;

import com.blogApp.blogcommon.enums.PostStatus;
import com.blogApp.blogpost.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho Post
 * - Tạo các phương thức để thực hiện các thao tác với Post
 * - Sử dụng JPA Repository để tương tác với database
 * - Tìm kiếm bài viết theo slug @Optional<Post> findBySlug(String slug);
 * - Tìm kiếm bài viết theo status @Page<Post> findByStatus(PostStatus status, Pageable pageable);
 * - Tìm kiếm bài viết theo authorId @Page<Post> findByAuthorId(String authorId, Pageable pageable);
 * - Tìm kiếm bài viết theo categoryId và status @Page<Post> findByCategoryIdAndStatus(@Param("categoryId") UUID categoryId, @Param("status") PostStatus status, Pageable pageable);
 * - Tìm kiếm bài viết theo tagId và status @Page<Post> findByTagIdAndStatus(@Param("tagId") UUID tagId, @Param("status") PostStatus status, Pageable pageable);
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlug(String slug);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByAuthorId(String authorId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.id = :categoryId AND p.status = :status")
    Page<Post> findByCategoryIdAndStatus(@Param("categoryId") UUID categoryId, @Param("status") PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.id = :tagId AND p.status = :status")
    Page<Post> findByTagIdAndStatus(@Param("tagId") UUID tagId, @Param("status") PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% OR p.summary LIKE %:keyword%")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY p.publishedAt DESC")
    Page<Post> findLatestPosts(@Param("status") PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY p.viewCount DESC")
    Page<Post> findPopularPosts(@Param("status") PostStatus status, Pageable pageable);

    boolean existsBySlug(String slug);
}
