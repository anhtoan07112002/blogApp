package com.blogApp.blogpost.service.interfaces;

import com.blogApp.blogcommon.dto.response.PagedResponse;
import com.blogApp.blogcommon.enums.PostStatus;
import com.blogApp.blogpost.dto.request.PostCreateRequest;
import com.blogApp.blogpost.dto.request.PostUpdateRequest;
import com.blogApp.blogpost.dto.response.PostSummaryDTO;

import java.util.UUID;

/**
 * Interface cho PostService
 * - Tạo bài viết mới
 * - Lấy bài viết theo ID
 * - Lấy bài viết theo slug
 * - Cập nhật bài viết
 * - Xóa bài viết
 * - Lấy tất cả bài viết
 * - Lấy bài viết theo status
 * - Lấy bài viết theo authorId
 * - Lấy bài viết theo categoryId
 * - Lấy bài viết theo tagId
 */
public interface PostService {

    /**
     * Tạo bài viết mới
     * @param createPostDTO Thông tin bài viết
     * @param userId ID của người tạo bài viết
     * @return PostSummaryDTO chứa thông tin bài viết đã tạo
     */
    PostSummaryDTO createPost(PostCreateRequest createPostDTO, String userId);

    /**
     * Lấy bài viết theo ID
     * @param id ID của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết
     */
    PostSummaryDTO getPostById(UUID id);

    /**
     * Lấy bài viết theo slug
     * @param slug slug của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết
     */
    PostSummaryDTO getPostBySlug(String slug);

    /**
     * Cập nhật bài viết
     * @param id ID của bài viết
     * @param updatePostDTO Thông tin bài viết mới
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */
    PostSummaryDTO updatePost(UUID id, PostUpdateRequest updatePostDTO);

    /**
     * Xóa bài viết
     * @param id ID của bài viết
     */
    void deletePost(UUID id);

    /**
     * Lấy tất cả bài viết
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Tên trường sắp xếp
     * @param sortDir Hướng sắp xếp 
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    PagedResponse<PostSummaryDTO> getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Lấy bài viết theo authorId
     * @param authorId ID của author
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Tên trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    PagedResponse<PostSummaryDTO> getPostsByAuthor(String authorId, int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Lấy bài viết theo categoryId
     * @param categoryId ID của category
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Tên trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    PagedResponse<PostSummaryDTO> getPostsByCategory(UUID categoryId, int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Lấy bài viết theo status
     * @param status Trạng thái bài viết
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Tên trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    PagedResponse<PostSummaryDTO> getPostsByStatus(PostStatus status, int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Lấy bài viết theo tagId
     * @param tagId ID của tag
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param sortBy Tên trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    PagedResponse<PostSummaryDTO> getPostsByTag(UUID tagId, int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Tìm kiếm bài viết
     * @param keyword Từ khóa tìm kiếm
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @return PagedResponse<PostSummaryDTO> chứa danh sách bài viết
     */
    PagedResponse<PostSummaryDTO> searchPosts(String keyword, int pageNo, int pageSize);

    /**
     * Cập nhật trạng thái bài viết
     * @param id ID của bài viết
     * @param status Trạng thái bài viết
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */
    PostSummaryDTO updatePostStatus(UUID id, PostStatus status);

    /**
     * Tăng số lượt xem bài viết
     * @param id ID của bài viết
     * @return PostSummaryDTO chứa thông tin bài viết đã cập nhật
     */ 
    PostSummaryDTO incrementViewCount(UUID id);
}
