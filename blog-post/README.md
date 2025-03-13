# Blog Post Service

Blog Post Service là module quản lý bài viết, danh mục, thẻ và bình luận cho hệ thống Blog. Module này là một phần của kiến trúc microservices và cung cấp các API để tạo, đọc, cập nhật, xóa (CRUD) và tìm kiếm các loại nội dung.

## Tính năng chính

- **Quản lý bài viết**: Tạo, đọc, cập nhật, xóa và tìm kiếm bài viết
- **Quản lý danh mục**: Tổ chức bài viết theo danh mục
- **Quản lý thẻ**: Gắn thẻ cho bài viết để dễ tìm kiếm
- **Hệ thống bình luận**: Cho phép người dùng bình luận vào bài viết
- **Tìm kiếm và lọc**: Tìm kiếm bài viết theo nhiều tiêu chí
- **Phân trang và sắp xếp**: Hỗ trợ phân trang và nhiều cách sắp xếp kết quả
- **Xử lý Markdown**: Chuyển đổi nội dung Markdown sang HTML
- **Cache**: Tối ưu hiệu suất bằng caching

## API Endpoints chính

- **Bài viết (Posts)**:
  - `GET /posts`: Lấy danh sách bài viết
  - `GET /posts/{id}`: Lấy chi tiết bài viết theo ID
  - `GET /posts/slug/{slug}`: Lấy bài viết theo slug
  - `POST /posts`: Tạo bài viết mới
  - `PUT /posts/{id}`: Cập nhật bài viết
  - `DELETE /posts/{id}`: Xóa bài viết
  - `GET /posts/search`: Tìm kiếm bài viết

- **Danh mục (Categories)**:
  - `GET /categories`: Lấy tất cả danh mục
  - `GET /categories/{id}`: Lấy danh mục theo ID
  - `POST /categories`: Tạo danh mục mới
  - `PUT /categories/{id}`: Cập nhật danh mục
  - `DELETE /categories/{id}`: Xóa danh mục
  - `GET /categories/{id}/posts`: Lấy bài viết theo danh mục

- **Thẻ (Tags)**:
  - `GET /tags`: Lấy tất cả thẻ
  - `GET /tags/{id}`: Lấy thẻ theo ID
  - `POST /tags`: Tạo thẻ mới
  - `PUT /tags/{id}`: Cập nhật thẻ
  - `DELETE /tags/{id}`: Xóa thẻ
  - `GET /tags/{id}/posts`: Lấy bài viết theo thẻ

- **Bình luận (Comments)**:
  - `GET /posts/{postId}/comments`: Lấy bình luận của bài viết
  - `POST /posts/{postId}/comments`: Thêm bình luận mới
  - `PUT /posts/{postId}/comments/{commentId}`: Cập nhật bình luận
  - `DELETE /posts/{postId}/comments/{commentId}`: Xóa bình luận

## Công nghệ sử dụng

- **Spring Boot**: Framework chính
- **Spring Data JPA**: Tương tác với cơ sở dữ liệu
- **Spring Cloud OpenFeign**: Gọi API từ các service khác
- **MySQL**: Cơ sở dữ liệu lưu trữ bài viết và dữ liệu liên quan
- **Redis**: Cache để tối ưu hiệu suất
- **Lucene**: Tìm kiếm toàn văn
- **CommonMark**: Xử lý Markdown
- **Swagger/OpenAPI**: Tạo tài liệu API tự động

## Cài đặt và Cấu hình

### 1. Chuẩn bị môi trường

- Java 17+
- MySQL 8.0
- Redis 7.0 (tùy chọn, cho caching)
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
       url: jdbc:mysql://localhost:3306/blog_post_db?useSSL=false&serverTimezone=UTC
       username: your-db-username
       password: your-db-password
   ```

   b. Redis configuration:
   ```yaml
   spring:
     redis:
       host: localhost
       port: 6379
       password: your-redis-password  # Nếu có
   ```

   c. Auth Service configuration:
   ```yaml
   app:
     auth-service:
       url: http://localhost:8081
   ```

   d. Cache configuration:
   ```yaml
   spring:
     cache:
       type: redis
       redis:
         time-to-live: 3600000  # 1 giờ, tính bằng milliseconds
   ```

   e. Lucene index location:
   ```yaml
   app:
     lucene:
       indexDir: /path/to/lucene/index
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

#### Chạy với Docker

```bash
# Build docker image
docker build -t blog-post-service .

# Chạy container
docker run -p 8082:8082 --name blog-post --network blog-network blog-post-service
```

## Mô hình dữ liệu

### Post (Bài viết)
- **id**: Định danh duy nhất (UUID)
- **title**: Tiêu đề bài viết
- **slug**: URL-friendly string
- **content**: Nội dung bài viết (Markdown)
- **contentHtml**: Nội dung chuyển đổi sang HTML
- **excerpt**: Tóm tắt ngắn gọn
- **status**: Trạng thái (DRAFT, PUBLISHED, ARCHIVED)
- **authorId**: ID của tác giả
- **authorName**: Tên tác giả (cache để giảm lượng gọi đến Auth Service)
- **categories**: Danh sách các danh mục
- **tags**: Danh sách các thẻ
- **viewCount**: Số lượt xem
- **publishedAt**: Thời gian xuất bản
- **createdAt**: Thời gian tạo
- **updatedAt**: Thời gian cập nhật gần nhất

### Category (Danh mục)
- **id**: Định danh duy nhất (UUID)
- **name**: Tên danh mục
- **slug**: URL-friendly string
- **description**: Mô tả
- **createdAt**: Thời gian tạo
- **updatedAt**: Thời gian cập nhật gần nhất

### Tag (Thẻ)
- **id**: Định danh duy nhất (UUID)
- **name**: Tên thẻ
- **slug**: URL-friendly string
- **createdAt**: Thời gian tạo
- **updatedAt**: Thời gian cập nhật gần nhất

### Comment (Bình luận)
- **id**: Định danh duy nhất (UUID)
- **content**: Nội dung bình luận
- **postId**: ID của bài viết
- **userId**: ID của người bình luận
- **userName**: Tên người bình luận
- **parentId**: ID của bình luận cha (nếu là phản hồi)
- **createdAt**: Thời gian tạo
- **updatedAt**: Thời gian cập nhật gần nhất

## Xử lý Markdown và HTML

Blog Post Service tự động chuyển đổi nội dung Markdown trong bài viết sang HTML để hiển thị. Quá trình này diễn ra khi:

1. Tạo bài viết mới
2. Cập nhật nội dung bài viết
3. Lấy bài viết (nếu HTML chưa được tạo)

Thư viện CommonMark được sử dụng để xử lý Markdown với các tính năng như:
- Heading, paragraphs
- Lists, blockquotes
- Code blocks (với syntax highlighting)
- Links và images
- Tables
- Và nhiều tính năng Markdown khác

## Tích hợp với các module khác

Blog Post Service tương tác với các service khác như sau:

1. **Auth Service**: Xác thực người dùng và lấy thông tin tác giả
2. **Media Service**: Quản lý media được nhúng trong bài viết

## Bảo mật và Hiệu suất

### Bảo mật
- Xác thực JWT thông qua Auth Service
- Kiểm tra quyền hạn trước khi thực hiện các hoạt động (RBAC)
- Kiểm soát truy cập vào bài viết dựa trên trạng thái và quyền sở hữu

### Caching
- Cache danh sách bài viết
- Cache bài viết theo ID và slug
- Cache danh mục và thẻ
- Invalidate cache khi có thay đổi

### Tối ưu hiệu suất
- Phân trang để giảm dung lượng phản hồi
- Lazy loading cho các mối quan hệ phức tạp
- Sử dụng Redis cho caching
- Index cơ sở dữ liệu cho các truy vấn phổ biến

## Kiểm tra Service

Sau khi khởi động, bạn có thể kiểm tra service:

1. Swagger UI: http://localhost:8082/api/swagger-ui
2. API Docs: http://localhost:8082/api/api-docs
3. Health Check: http://localhost:8082/api/actuator/health

## Xử lý sự cố

1. **Lỗi kết nối Database**:
   - Kiểm tra cấu hình database trong `application.yml`
   - Đảm bảo MySQL đã được khởi động và có thể truy cập

2. **Lỗi kết nối Redis**:
   - Kiểm tra cấu hình Redis trong `application.yml`
   - Service vẫn hoạt động mà không có caching nếu Redis không khả dụng

3. **Lỗi kết nối Auth Service**:
   - Đảm bảo Auth Service đang chạy và có thể truy cập
   - Kiểm tra URL của Auth Service trong cấu hình

4. **Lucene Index**:
   - Đảm bảo thư mục index có quyền đọc/ghi
   - Nếu tìm kiếm không hoạt động, thử xây dựng lại index 