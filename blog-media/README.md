# Blog Media Service

Blog Media Service là module quản lý tất cả các tệp media (hình ảnh, video, âm thanh, tài liệu) cho hệ thống Blog. Module này là một phần của kiến trúc microservices và cung cấp các API để upload, lưu trữ, truy xuất và quản lý các tệp media.

## Tính năng chính

- **Upload và lưu trữ file**: Cho phép upload các loại tệp media khác nhau (hình ảnh, video, âm thanh, tài liệu)
- **Quản lý metadata**: Lưu trữ và quản lý metadata của tệp media (kích thước, loại, tên, v.v.)
- **Liên kết với bài viết**: Kết nối tệp media với bài viết
- **Kiểm tra và xác thực**: Kiểm tra loại file, kích thước, và nội dung
- **Xử lý hình ảnh**: Tạo thumbnail, định dạng lại và tối ưu hình ảnh
- **Lưu trữ đám mây**: Tích hợp với MinIO (hoặc S3) để lưu trữ tệp media
- **Bảo mật truy cập**: Kiểm soát quyền truy cập vào tệp media

## API Endpoints chính

- **Media Management**:
  - `POST /api/v1/media/upload`: Upload file media
  - `POST /api/v1/media/upload/with-metadata`: Upload file media với metadata
  - `GET /api/v1/media/{id}`: Lấy thông tin về media
  - `GET /api/v1/media/{id}/content`: Tải xuống nội dung media
  - `GET /api/v1/media/{id}/url`: Lấy URL để truy cập media
  - `DELETE /api/v1/media/{id}`: Xóa media
  - `PUT /api/v1/media/{id}/metadata`: Cập nhật metadata của media

- **Media-Post Relationship**:
  - `POST /api/v1/media/post/{mediaId}/post/{postId}`: Thêm media vào bài viết
  - `DELETE /api/v1/media/post/{mediaId}/post/{postId}`: Xóa media khỏi bài viết
  - `GET /api/v1/media/post/{postId}`: Lấy danh sách media của bài viết

## Công nghệ sử dụng

- **Spring Boot**: Framework chính
- **Spring Data JPA**: Tương tác với cơ sở dữ liệu
- **PostgreSQL**: Cơ sở dữ liệu để lưu trữ metadata
- **MinIO Client**: Tích hợp với MinIO cho lưu trữ object
- **Apache Tika**: Phát hiện và xác thực loại file
- **Thumbnailator**: Xử lý và tối ưu hình ảnh
- **Spring Cloud OpenFeign**: Gọi API từ các service khác
- **Swagger/OpenAPI**: Tạo tài liệu API tự động

## Cài đặt và Cấu hình

### 1. Chuẩn bị môi trường

- Java 17+
- PostgreSQL 15+
- MinIO Server (hoặc tương thích S3)
- Blog Auth Service (để xác thực người dùng)

### 2. Cấu hình

1. Copy file cấu hình mẫu:
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

2. Cập nhật các thông tin trong `application.yml`:

   a. Database configuration:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/blog_media_db
       username: your-db-username
       password: your-db-password
   ```

   b. MinIO configuration:
   ```yaml
   minio:
     url: http://localhost:9000
     access-key: minioadmin
     secret-key: minioadmin
     bucket-name: blog-media
   ```

   c. Maximum file size:
   ```yaml
   spring:
     servlet:
       multipart:
         max-file-size: 10MB
         max-request-size: 10MB
   ```

   d. Auth Service configuration:
   ```yaml
   app:
     auth-service:
       url: http://localhost:8081
   ```

### 3. Chạy ứng dụng

#### Chạy trực tiếp

```bash
# Development
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Test
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

#### Chạy với Docker và Docker Compose

Blog Media Service có file docker-compose.yml riêng để cấu hình MinIO kèm theo:

```bash
# Khởi động PostgreSQL và MinIO
docker-compose up -d postgres minio

# Khởi động Media Service
docker-compose up -d blog-media
```

### 4. Khởi tạo MinIO Bucket

Khi khởi động lần đầu, service sẽ tự động tạo bucket trong MinIO nếu nó chưa tồn tại. Nếu bạn muốn tạo bucket thủ công:

```bash
# Sử dụng MinIO Client
mc config host add myminio http://localhost:9000 minioadmin minioadmin
mc mb myminio/blog-media
```

## Mô hình dữ liệu

### Media
- **id**: Định danh duy nhất (UUID)
- **fileName**: Tên file gốc
- **storagePath**: Đường dẫn lưu trữ trong MinIO/S3
- **contentType**: MIME type của file
- **fileSize**: Kích thước file (bytes)
- **mediaFileType**: Loại media (IMAGE, VIDEO, AUDIO, DOCUMENT)
- **publicUrl**: URL công khai để truy cập file
- **metaData**: JSON chứa metadata của file
- **createdAt**: Thời gian tạo
- **updatedAt**: Thời gian cập nhật gần nhất

### MediaPost
- **id**: Định danh duy nhất (UUID)
- **mediaId**: ID của media
- **postId**: ID của bài viết liên kết
- **type**: Loại media trong bài viết (thumbnail, cover, content)
- **position**: Vị trí của media trong bài viết
- **createdAt**: Thời gian tạo
- **updatedAt**: Thời gian cập nhật gần nhất

## Loại file hỗ trợ

Blog Media Service hỗ trợ nhiều loại file, được phân loại thành các nhóm:

1. **IMAGE**:
   - PNG, JPEG, GIF, WebP, SVG
   - Giới hạn kích thước: 5MB

2. **VIDEO**:
   - MP4, WebM, AVI, MOV
   - Giới hạn kích thước: 100MB

3. **AUDIO**:
   - MP3, WAV, OGG, AAC
   - Giới hạn kích thước: 20MB

4. **DOCUMENT**:
   - PDF, DOCX, XLSX, PPTX, TXT, Markdown
   - Giới hạn kích thước: 10MB

## Xử lý và Tối ưu Hình ảnh

Blog Media Service tự động xử lý hình ảnh khi upload:

1. **Thumbnail Generation**: Tạo thumbnail cho hình ảnh
2. **Format Optimization**: Chuyển đổi định dạng nếu cần
3. **Size Optimization**: Nén và tối ưu kích thước
4. **Metadata Extraction**: Trích xuất metadata từ hình ảnh (kích thước, định dạng, v.v.)

## Tích hợp với các module khác

Blog Media Service tương tác với các service khác như sau:

1. **Auth Service**: Xác thực người dùng và kiểm tra quyền truy cập
2. **Post Service**: Liên kết media với bài viết

## Bảo mật và Kiểm soát Truy cập

- **Xác thực JWT**: Xác thực người dùng thông qua Auth Service
- **CORS Control**: Kiểm soát domain được phép truy cập API
- **File Type Validation**: Kiểm tra và xác thực loại file
- **Virus Scanning**: (Tùy chọn) Quét virus cho file được upload
- **Access Control**: Kiểm soát quyền truy cập vào file dựa trên quyền sở hữu

## Kiểm tra Service

Sau khi khởi động, bạn có thể kiểm tra service:

1. Swagger UI: http://localhost:8083/api/media/swagger-ui
2. API Docs: http://localhost:8083/api/media/api-docs
3. Health Check: http://localhost:8083/api/media/actuator/health
4. MinIO Console: http://localhost:9001 (username: minioadmin, password: minioadmin)

## Xử lý sự cố

1. **Lỗi kết nối Database**:
   - Kiểm tra cấu hình PostgreSQL trong `application.yml`
   - Đảm bảo PostgreSQL đã được khởi động và có thể truy cập

2. **Lỗi kết nối MinIO**:
   - Kiểm tra cấu hình MinIO trong `application.yml`
   - Đảm bảo MinIO Server đã được khởi động
   - Kiểm tra access key và secret key

3. **Lỗi upload file**:
   - Kiểm tra giới hạn kích thước file trong cấu hình
   - Kiểm tra loại file có được hỗ trợ không
   - Kiểm tra quyền ghi vào MinIO/bucket

4. **Lỗi kết nối Auth Service**:
   - Đảm bảo Auth Service đang chạy
   - Kiểm tra URL của Auth Service trong cấu hình

## Triển khai sản xuất

Khi triển khai vào môi trường sản xuất, hãy lưu ý:

1. **Bảo mật MinIO**: Thay đổi thông tin đăng nhập mặc định
2. **HTTPS**: Sử dụng HTTPS cho tất cả API endpoint và URL của media
3. **Monitoring**: Giám sát dung lượng lưu trữ và hoạt động của service
4. **Backup**: Sao lưu dữ liệu PostgreSQL và nội dung MinIO
5. **Scaling**: Cân nhắc sử dụng S3 hoặc MinIO cluster cho khả năng mở rộng 