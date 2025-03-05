package com.blogApp.blogpost.mapper;

import com.blogApp.blogcommon.dto.TagDTO;
import com.blogApp.blogpost.dto.request.TagCreateRequest;
import com.blogApp.blogpost.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Interface mapper để chuyển đổi giữa các đối tượng liên quan đến Tag
 * - Chuyển đổi giữa entity và các DTO
 * - Tự động bỏ qua các trường tự sinh
 * - Đơn giản hóa việc chuyển đổi dữ liệu
 */
@Mapper(componentModel = "spring")
public interface TagMapper {

    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    /**
     * Chuyển từ request sang entity khi tạo mới
     * Bỏ qua các trường tự sinh
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "posts", ignore = true)
    Tag toEntity(TagCreateRequest request);

    TagDTO toDto(Tag tag);
}
