package com.blogApp.blogpost.mapper;

import com.blogApp.blogcommon.dto.CategorySummaryDTO;
import com.blogApp.blogcommon.dto.PostSummaryDTO;
import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogpost.dto.request.PostCreateRequest;
import com.blogApp.blogpost.dto.request.PostUpdateRequest;
import com.blogApp.blogpost.dto.response.PostDetailDTO;
import com.blogApp.blogpost.model.Category;
import com.blogApp.blogpost.model.Post;
import com.blogApp.blogpost.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interface mapper để chuyển đổi giữa các đối tượng liên quan đến Post
 * - Chuyển đổi giữa entity và các DTO
 * - Hỗ trợ mapping phức tạp cho categories và tags
 * - Sử dụng MapStruct để tự động sinh code
 * - Có các phương thức helper để xử lý collection
 */
@Mapper(componentModel = "spring")
public interface PostMapper {

    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    /**
     * Chuyển từ request sang entity khi tạo mới
     * Bỏ qua các trường tự sinh
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Post toEntity(PostCreateRequest request);

    /**
     * Cập nhật entity từ request
     * Bỏ qua các trường không được phép cập nhật
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateEntityFromDto(PostUpdateRequest request, @MappingTarget Post post);

    /**
     * Chuyển sang DTO chi tiết
     * Map categories và tags thành các DTO tương ứng
     */
    @Mapping(target = "categories", expression = "java(mapCategoriesToSummaries(post.getCategories()))")
    @Mapping(target = "tags", expression = "java(mapTagsToDto(post.getTags()))")
    PostDetailDTO toDetailDto(Post post);

    /**
     * Chuyển sang DTO tóm tắt
     * Map categories và tags thành các DTO tương ứng
     */
    @Mapping(target = "categories", expression = "java(mapCategoriesToSummaries(post.getCategories()))")
    @Mapping(target = "tags", expression = "java(mapTagsToDto(post.getTags()))")
    PostSummaryDTO toSummaryDto(Post post);

    /**
     * Helper method để map danh sách Category sang CategorySummaryDto
     */
    default Set<CategorySummaryDTO> mapCategoriesToSummaries(Set<Category> categories) {
        if (categories == null) {
            return Set.of();
        }
        return categories.stream()
                .map(category -> new CategorySummaryDTO(
                        category.getId(),
                        category.getName(),
                        category.getSlug()))
                .collect(Collectors.toSet());
    }

    /**
     * Helper method để map danh sách Tag sang TagDto
     */
    default Set<TagDTO> mapTagsToDto(Set<Tag> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream()
                .map(tag -> new TagDTO(
                        tag.getId(),
                        tag.getName(),
                        tag.getSlug()))
                .collect(Collectors.toSet());
    }
}