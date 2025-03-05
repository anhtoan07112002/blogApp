package com.blogApp.blogpost.repository;

import com.blogApp.blogpost.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho Tag
 * - Tạo các phương thức để thực hiện các thao tác với Tag
 * - Sử dụng JPA Repository để tương tác với database
 * - Tìm kiếm tag theo name @Optional<Tag> findByName(String name);
 * - Tìm kiếm tag theo slug @Optional<Tag> findBySlug(String slug);
 * - Kiểm tra sự tồn tại của name @boolean existsByName(String name);
 * - Kiểm tra sự tồn tại của slug @boolean existsBySlug(String slug);
 * - Tìm kiếm tag phổ biến @Page<Tag> findPopularTags(Pageable pageable);
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String name);

    Optional<Tag> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @Query("SELECT t FROM Tag t JOIN t.posts p WHERE p.status = 'PUBLISHED' GROUP BY t.id ORDER BY COUNT(p.id) DESC")
    Page<Tag> findPopularTags(Pageable pageable);
}