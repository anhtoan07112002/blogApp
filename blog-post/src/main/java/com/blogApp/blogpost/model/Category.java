package com.blogApp.blogpost.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity đại diện cho một danh mục trong blog
 * - Sử dụng UUID làm khóa chính
 * - Hỗ trợ cấu trúc phân cấp (parent-child)
 * - Có mối quan hệ nhiều-nhiều với Post
 * - Tự động quản lý thời gian tạo/cập nhật
 * - Đảm bảo slug là duy nhất
 */
@Entity
@Table(name = "categories")
@Indexed
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Category {
    /**
     * ID của danh mục
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Tên của danh mục
     */
    @FullTextField
    @Column(nullable = false)
    private String name;

    /**
     * Slug của danh mục
     */
    @Column(nullable = false, unique = true)
    private String slug;

    /**
     * Mô tả của danh mục
     */
    @FullTextField
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Danh mục cha
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * Danh sách các danh mục con
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Category> children = new HashSet<>();

    /**
     * Danh sách các bài viết thuộc danh mục
     */ 
    @ManyToMany(mappedBy = "categories")
    private Set<Post> posts = new HashSet<>();

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
