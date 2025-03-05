package com.blogApp.blogmedia.service.interfaces;

import com.blogApp.blogmedia.exception.MediaProcessingException;
import com.blogApp.blogmedia.exception.MediaUploadException;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import io.minio.StatObjectResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface cho việc tương tác với MinIO storage
 * Cung cấp các phương thức cơ bản để:
 * - Upload file
 * - Download file
 * - Xóa file
 * - Kiểm tra file tồn tại
 * - Tạo URL tạm thời
 * - Lấy URL công khai
 */
public interface MinioService {

    /**
     * Upload file lên MinIO storage
     * 
     * @param file File cần upload (MultipartFile)
     * @param objectName Tên object trong MinIO
     * @param contentType Loại nội dung của file
     * @param metadata Metadata bổ sung để lưu cùng file
     * @return URL để truy cập file
     * @throws MediaUploadException khi có lỗi trong quá trình upload
     */
    String uploadFile(MultipartFile file, String objectName, String contentType, Map<String, String> metadata);
    
    /**
     * Upload InputStream lên MinIO storage
     * 
     * @param inputStream Stream dữ liệu file
     * @param objectName Tên object trong MinIO
     * @param size Kích thước file (bytes)
     * @param contentType Loại nội dung của file
     * @param metadata Metadata bổ sung để lưu cùng file
     * @return URL để truy cập file
     * @throws MediaUploadException khi có lỗi trong quá trình upload
     */
    String uploadFile(InputStream inputStream, String objectName, long size, String contentType, Map<String, String> metadata);
    
    /**
     * Lấy file từ MinIO storage
     * 
     * @param objectName Tên object trong MinIO
     * @return InputStream chứa dữ liệu file
     * @throws ResourceNotFoundException khi không tìm thấy file
     * @throws MediaProcessingException khi có lỗi trong quá trình đọc file
     */
    InputStream getFile(String objectName);
    
    /**
     * Lấy thông tin object từ MinIO
     * 
     * @param objectName Tên object trong MinIO
     * @return StatObjectResponse chứa metadata của object
     * @throws ResourceNotFoundException khi không tìm thấy object
     * @throws MediaProcessingException khi có lỗi trong quá trình lấy thông tin
     */
    StatObjectResponse getObjectInfo(String objectName);

    /**
     * Xóa file từ MinIO storage
     * 
     * @param objectName Tên object trong MinIO
     * @return true nếu xóa thành công, false nếu không
     * @throws ResourceNotFoundException khi không tìm thấy file
     * @throws MediaProcessingException khi có lỗi trong quá trình xóa
     */
    boolean deleteFile(String objectName);
    
    /**
     * Kiểm tra file có tồn tại trong MinIO không
     * 
     * @param objectName Tên object trong MinIO
     * @return true nếu file tồn tại, false nếu không
     * @throws MediaProcessingException khi có lỗi trong quá trình kiểm tra
     */
    boolean doesObjectExist(String objectName);
    
    /**
     * Tạo URL tạm thời để truy cập object
     * 
     * @param objectName Tên object trong MinIO
     * @param expirySeconds Thời gian URL có hiệu lực (giây)
     * @return URL tạm thời
     * @throws MediaProcessingException khi có lỗi trong quá trình tạo URL
     */
    String generatePresignedUrl(String objectName, int expirySeconds);
    
    /**
     * Lấy URL công khai của object
     * 
     * @param objectName Tên object trong MinIO
     * @return URL công khai để truy cập file
     * @throws MediaProcessingException khi có lỗi trong quá trình tạo URL
     */
    String getPublicUrl(String objectName);

    /**
     * Lấy tên bucket
     * 
     * @return Tên bucket
     */
    String getBucketName();
}
