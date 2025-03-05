package com.blogApp.blogpost.service.interfaces;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.dto.TagDTO;

import java.util.List;
import java.util.UUID;

public interface TagService {

    /**
     * Tạo tag mới
     * @param tagDTO Thông tin tag
     * @return TagDTO chứa thông tin tag đã tạo
     */ 
    TagDTO createTag(TagDTO tagDTO);

    /**
     * Lấy tag theo ID
     * @param id ID của tag
     * @return TagDTO chứa thông tin tag
     */ 
    TagDTO getTagById(UUID id);

    /**
     * Lấy tag theo slug
     * @param slug Slug của tag
     * @return TagDTO chứa thông tin tag
     */ 
    TagDTO getTagBySlug(String slug);

    /**
     * Cập nhật tag
     * @param id ID của tag
     * @param tagDTO Thông tin tag  
     * @return TagDTO chứa thông tin tag đã cập nhật
     */ 
    TagDTO updateTag(UUID id, TagDTO tagDTO);
 
    /**
     * Xóa tag
     * @param id ID của tag
     */     
    void deleteTag(UUID id);

    /**
     * Lấy tất cả tag
     * @return List<TagDTO> chứa danh sách tag
     */ 
    List<TagDTO> getAllTags();
  
    /**
     * Lấy tag phổ biến
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<TagDTO> chứa danh sách tag phổ biến
     */     
    PagedResponse<TagDTO> getPopularTags(int pageNo, int pageSize);
}
