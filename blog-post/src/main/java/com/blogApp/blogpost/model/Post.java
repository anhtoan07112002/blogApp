package com.blogApp.blogpost.model;

import com.blogApp.blogcommon.enums.PostStatus;
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
 * Entity đại diện cho một bài viết trong blog
 * - Sử dụng UUID làm khóa chính
 * - Có các trường cơ bản: tiêu đề, nội dung, slug, tác giả
 * - Hỗ trợ tìm kiếm full-text với Hibernate Search
 * - Có các mối quan hệ nhiều-nhiều với Category và Tag
 * - Có mối quan hệ một-nhiều với Comment
 * - Tự động quản lý thời gian tạo/cập nhật
 */
@Entity
@Table(name = "posts")
@Indexed
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @FullTextField
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @FullTextField
    @Column(columnDefinition = "TEXT")
    private String summary;

    @FullTextField
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String authorId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    @Column(nullable = false)
    private boolean commentEnabled;

    @Column
    @Builder.Default
    private Integer viewCount = 0;

    @Column
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_categories",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @Builder.Default
    private boolean isPublished = false;

    public void addCategory(Category category) {
        categories.add(category);
        category.getPosts().add(this);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
        category.getPosts().remove(this);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getPosts().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getPosts().remove(this);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }
}