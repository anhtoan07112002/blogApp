package com.blogApp.blogpost.mapper;

import com.blogApp.blogcommon.dto.CategorySummaryDTO;
import com.blogApp.blogpost.dto.CategoryDTO;
import com.blogApp.blogpost.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interface mapper để chuyển đổi giữa các đối tượng liên quan đến Category
 * - Chuyển đổi giữa entity và các DTO
 * - Hỗ trợ mapping cho cấu trúc phân cấp (parent-child)
 * - Tính toán số lượng bài viết trong danh mục
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    /**
     * Chuyển từ DTO sang entity khi tạo mới
     * Bỏ qua các trường tự sinh và quan hệ
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "posts", ignore = true)
    Category toEntity(CategoryDTO categoryDTO);

    /**
     * Chuyển sang DTO đầy đủ
     * Map thông tin về parent và children
     * Tính số lượng bài viết
     */
    @Mapping(target = "parentName", expression = "java(category.getParent() != null ? category.getParent().getName() : null)")
    @Mapping(target = "children", expression = "java(mapChildrenToSummaries(category.getChildren()))")
    CategoryDTO toDto(Category category);

    /**
     * Chuyển sang DTO tóm tắt
     * Chỉ bao gồm thông tin cơ bản
     */
    CategorySummaryDTO toSummaryDto(Category category);

    /**
     * Helper method để map danh sách category con sang DTO tóm tắt
     */
    default Set<CategorySummaryDTO> mapChildrenToSummaries(Set<Category> children) {
        if (children == null) {
            return Set.of();
        }
        return children.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toSet());
    }
}