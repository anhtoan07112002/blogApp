# Blog Authentication Service

Blog Authentication Service là module quản lý xác thực và phân quyền cho hệ thống Blog, là một phần của kiến trúc microservices. Module này cung cấp các API để đăng ký, đăng nhập, lấy thông tin người dùng và quản lý token.

## Tính năng chính

- **Quản lý người dùng**: Đăng ký, cập nhật thông tin và quản lý tài khoản
- **Xác thực**: Đăng nhập với JWT (JSON Web Token)
- **Quản lý Token**: Token truy cập và refresh token
- **Phân quyền**: Phân quyền dựa trên role-based access control (RBAC)
- **Tích hợp Email**: Gửi email xác thực và khôi phục mật khẩu
- **Caching**: Sử dụng Redis để cache token và thông tin người dùng
- **Security Headers**: Cấu hình bảo mật CORS, XSS và các header bảo mật khác

## API Endpoints chính

- **`POST /login`**: Đăng nhập và nhận token
- **`POST /signup`**: Đăng ký tài khoản mới
- **`POST /refresh-token`**: Làm mới access token
- **`POST /signout`**: Đăng xuất và vô hiệu hóa token
- **`GET /me`**: Lấy thông tin người dùng hiện tại
- **`GET /users/{username}`**: Xem profile của người dùng
- **`PUT /users/password`**: Đổi mật khẩu
- **`POST /reset-password`**: Yêu cầu đặt lại mật khẩu

## Công nghệ sử dụng

- **Spring Boot**: Framework chính
- **Spring Security**: Xử lý xác thực và phân quyền
- **JWT (JSON Web Token)**: Để xác thực stateless
- **Spring Data JPA**: Tương tác với cơ sở dữ liệu
- **MySQL**: Cơ sở dữ liệu lưu trữ thông tin người dùng
- **Redis**: Cache và lưu trữ token
- **Spring Mail**: Gửi email

## Cài đặt và Cấu hình

### 1. Chuẩn bị môi trường

- Java 17+
- MySQL 8.0
- Redis 7.0 (tùy chọn, cho caching)

### 2. Cấu hình

1. Copy file cấu hình mẫu:
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

2. Cập nhật các thông tin trong `application.yml`:

   a. Email configuration:
   ```yaml
   spring:
     mail:
       username: your-email@gmail.com
       password: your-app-password  # App password từ Google
   ```

   b. Database configuration:
   ```yaml
   spring:
     datasource:
       username: your-db-username
       password: your-db-password
   ```

   c. JWT configuration:
   ```yaml
   app:
     auth:
       jwt:
         secret: your-jwt-secret  # Nên sử dụng một chuỗi ngẫu nhiên đủ mạnh
   ```

   d. Redis configuration (nếu cần):
   ```yaml
   spring:
     redis:
       password: your-redis-password  # Nếu Redis yêu cầu mật khẩu
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
docker build -t blog-auth-service .

# Chạy container
docker run -p 8081:8081 --name blog-auth --network blog-network blog-auth-service
```

## Cách lấy App Password cho Gmail

1. Truy cập Google Account Settings (https://myaccount.google.com/)
2. Chọn Security > 2-Step Verification
3. Tại mục "App passwords", tạo mới một app password
4. Chọn ứng dụng là "Mail" và thiết bị tùy chọn
5. Copy password được tạo và dán vào `application.yml`

## Cấu hình cho môi trường khác nhau

Spring Boot hỗ trợ nhiều profile khác nhau cho các môi trường. Mỗi profile sẽ có file cấu hình riêng.

1. **Development (application-dev.yml)**:
   - `ddl-auto: update` - Tự động cập nhật schema
   - `show-sql: true` - Hiển thị SQL queries
   - Cấu hình log ở mức DEBUG
   - JWT hết hạn sau thời gian ngắn

2. **Production (application-prod.yml)**:
   - `ddl-auto: none` - Không tự động thay đổi schema
   - `show-sql: false` - Không hiển thị SQL queries
   - Cấu hình log ở mức INFO/ERROR
   - JWT hết hạn sau thời gian dài hơn
   - Bật caching

3. **Test (application-test.yml)**:
   - `ddl-auto: create-drop` - Tạo mới schema mỗi lần chạy test
   - Sử dụng H2 in-memory database
   - Cấu hình log ở mức DEBUG

## Bảo mật

### Lưu ý quan trọng

1. **Thông tin nhạy cảm:**
   - Không commit file `application.yml` lên git
   - Sử dụng App Password cho Gmail thay vì mật khẩu thông thường
   - Đảm bảo JWT secret đủ mạnh và độc nhất
   - Trong môi trường production, sử dụng biến môi trường thay vì hard-code

2. **JWT Security:**
   - Access token có thời hạn ngắn (mặc định 30 phút)
   - Refresh token có thời hạn dài hơn (mặc định 7 ngày)
   - Sử dụng ít nhất thuật toán HS256 cho JWT
   - Token bao gồm các claim cần thiết (userId, username, roles...)

3. **Password Storage:**
   - Mật khẩu được mã hóa bằng BCrypt
   - Không lưu trữ hoặc truyền mật khẩu dưới dạng plain text

## Tích hợp với các module khác

Blog Auth Service cấp quyền truy cập cho các service khác thông qua cơ chế JWT. Các service khác có thể:

1. Chuyển hướng người dùng đến Auth Service để đăng nhập
2. Nhận JWT token sau khi xác thực thành công
3. Gửi token này trong header Authorization khi gọi các API khác
4. Xác thực và kiểm tra quyền truy cập dựa trên token

## Kiểm tra Service

Sau khi khởi động, bạn có thể kiểm tra service:

1. Swagger UI: http://localhost:8081/api/auth/swagger-ui
2. API Docs: http://localhost:8081/api/auth/api-docs
3. Health Check: http://localhost:8081/api/auth/actuator/health

## Xử lý sự cố

1. **Lỗi kết nối Database**:
   - Kiểm tra URL, username và password trong `application.yml`
   - Đảm bảo Database đã khởi động và có thể kết nối từ ứng dụng

2. **Lỗi Redis**:
   - Nếu không thể kết nối Redis, service vẫn hoạt động nhưng không có caching
   - Kiểm tra cấu hình Redis trong `application.yml`

3. **Lỗi gửi email**:
   - Kiểm tra cấu hình SMTP trong `application.yml`
   - Đảm bảo App Password cho Gmail là chính xác

4. **Token hết hạn quá nhanh**:
   - Kiểm tra thời gian hết hạn của access token và refresh token trong cấu hình

5. **Lỗi CORS**:
   - Đảm bảo các domain được phép đã được thêm vào cấu hình CORS 