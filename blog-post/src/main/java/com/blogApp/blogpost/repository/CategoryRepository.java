package com.blogApp.blogpost.repository;

import com.blogApp.blogpost.model.Category;
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
 * Repository cho Category
 * - Tạo các phương thức để thực hiện các thao tác với Category
 * - Sử dụng JPA Repository để tương tác với database
 * - Tìm kiếm danh mục theo slug @Optional<Category> findBySlug(String slug);
 * - Kiểm tra sự tồn tại của slug @boolean existsBySlug(String slug);
 * - Tìm danh mục cha @List<Category> findByParentIsNull();
 * - Tìm danh mục con @List<Category> findByParentId(@Param("parentId") UUID parentId);
 * - Tìm danh mục phổ biến @Page<Category> findPopularCategories(Pageable pageable);
 * - Đếm số bài viết đã được đăng trong danh mục @long countPublishedPosts(@Param("categoryId") UUID categoryId);
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByParentIsNull();

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId")
    List<Category> findByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT DISTINCT c FROM Category c JOIN c.posts p WHERE p.status = 'PUBLISHED' GROUP BY c.id ORDER BY COUNT(p.id) DESC")
    Page<Category> findPopularCategories(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Category c JOIN c.posts p WHERE c.id = :categoryId AND p.status = 'PUBLISHED'")
    long countPublishedPosts(@Param("categoryId") UUID categoryId);
}
