# Blog Application

## Giới thiệu

Đây là ứng dụng Blog được xây dựng với kiến trúc microservices, bao gồm các dịch vụ:
- **blog-eureka**: Dịch vụ Service Discovery (Eureka Server)
- **blog-auth**: Dịch vụ xác thực và phân quyền
- **blog-common**: Thư viện chung cho các dịch vụ
- **blog-post**: Dịch vụ quản lý bài viết, danh mục, thẻ và bình luận
- **blog-media**: Dịch vụ quản lý media (hình ảnh, video, tài liệu)

## Tài liệu API (Swagger)

Swagger UI được tích hợp để cung cấp tài liệu API tương tác. Sau khi khởi động ứng dụng, bạn có thể truy cập Swagger UI tại:

- **Blog Auth Service**: http://localhost:8081/api/auth/swagger-ui
  - API Docs JSON: http://localhost:8081/api/auth/api-docs

- **Blog Post Service**: http://localhost:8082/api/swagger-ui
  - API Docs JSON: http://localhost:8082/api/api-docs

- **Blog Media Service**: http://localhost:8083/api/media/swagger-ui
  - API Docs JSON: http://localhost:8083/api/media/api-docs

### Sử dụng Swagger UI

1. Truy cập URL Swagger UI
2. Khám phá các API có sẵn
3. Thử nghiệm API:
   - Đối với API không yêu cầu xác thực, bạn có thể gọi trực tiếp
   - Đối với API yêu cầu xác thực:
     - Đăng nhập qua API `/login` của Auth Service để lấy token
     - Nhấp vào nút "Authorize" ở góc trên bên phải
     - Nhập token JWT (không cần tiền tố "Bearer ")
     - Sau đó bạn có thể gọi các API được bảo vệ

## Service Discovery (Eureka)

Eureka Server được sử dụng để đăng ký và khám phá các dịch vụ trong hệ thống. Sau khi khởi động, bạn có thể truy cập Eureka Dashboard tại:

- URL: http://localhost:8761

Tại đây, bạn có thể xem tất cả các dịch vụ đã đăng ký và trạng thái của chúng.

## Chạy ứng dụng với Docker

### Yêu cầu

- Docker và Docker Compose

### Khởi động toàn bộ hệ thống

```bash
# Khởi động tất cả các dịch vụ chính (Eureka, Auth, Post)
docker-compose up -d

# Khởi động Media Service
cd blog-media
docker-compose up -d
```

Lệnh này sẽ khởi động:
- Eureka Server
- MySQL cho blog-auth
- MySQL cho blog-post
- PostgreSQL cho blog-media
- Redis cho cache và session
- MinIO cho lưu trữ media
- Blog Auth Service
- Blog Post Service
- Blog Media Service

### Quản lý MinIO

Sau khi khởi động, bạn có thể truy cập MinIO Console tại:
- URL: http://localhost:9001
- Username: minioadmin
- Password: minioadmin

### Kiểm tra trạng thái các container

```bash
docker-compose ps
cd blog-media && docker-compose ps
```

### Xem logs

```bash
# Xem logs của tất cả các dịch vụ
docker-compose logs
cd blog-media && docker-compose logs

# Xem logs của một dịch vụ cụ thể
docker-compose logs blog-eureka
docker-compose logs blog-auth
docker-compose logs blog-post
cd blog-media && docker-compose logs blog-media

# Xem logs theo thời gian thực
docker-compose logs -f blog-eureka
docker-compose logs -f blog-auth
docker-compose logs -f blog-post
cd blog-media && docker-compose logs -f blog-media
```

### Dừng hệ thống

```bash
docker-compose down
cd blog-media && docker-compose down
```

### Xóa volumes (dữ liệu)

```bash
docker-compose down -v
cd blog-media && docker-compose down -v
```

## Phát triển

### Cấu trúc dự án

```
blog/
├── blog-eureka/         # Service Discovery
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── blog-auth/           # Dịch vụ xác thực
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── blog-common/         # Thư viện chung
│   ├── src/
│   └── pom.xml
├── blog-post/           # Dịch vụ quản lý bài viết
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── blog-media/          # Dịch vụ quản lý media
│   ├── src/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── pom.xml
├── docker-compose.yml   # Cấu hình Docker Compose chính
├── .env                 # Biến môi trường
└── pom.xml              # POM cha
```

### Xây dựng ứng dụng

```bash
mvn clean package
```

### Chạy ứng dụng trong môi trường phát triển

```bash
# Chạy blog-eureka
cd blog-eureka
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Chạy blog-auth
cd blog-auth
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Chạy blog-post
cd blog-post
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Chạy blog-media
cd blog-media
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Thư viện chung (blog-common)

Blog Common là một thư viện chia sẻ được sử dụng bởi tất cả các dịch vụ khác. Nó chứa:

- Các lớp bảo mật chung
- Các tiện ích và hằng số
- Các DTO và model chung
- Các cấu hình chung
- Xử lý ngoại lệ
- Validation

Để sử dụng blog-common trong một dịch vụ, thêm dependency sau vào pom.xml:

```xml
<dependency>
    <groupId>com.blog</groupId>
    <artifactId>blog-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Cài đặt và Cấu hình

### 1. Yêu cầu hệ thống
- Java 17+
- Docker và Docker Compose
- MySQL 8.0
- Redis 7.0
- Maven

### 2. Thiết lập môi trường

#### 2.1. Cấu hình biến môi trường
1. Sao chép file `.env.example` thành `.env`:
```bash
cp .env.example .env
```

2. Cập nhật các giá trị trong file `.env`:
- Thay đổi các mật khẩu database
- Cấu hình JWT secret key (Base64 encoded)
- Thêm thông tin email và app password
- Cập nhật các thông tin kết nối khác

#### 2.2. Cấu hình application.yml cho các service
1. Trong mỗi service (blog-auth, blog-post, blog-media), sao chép file mẫu:
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

2. Cập nhật các thông tin cần thiết trong file `application.yml` của mỗi service

### 3. Khởi động ứng dụng
```bash
docker-compose up -d
```

### 4. Bảo mật

#### 4.1. Thông tin nhạy cảm
Các file sau đây chứa thông tin nhạy cảm và KHÔNG được commit lên Git:
- `.env`
- `src/main/resources/application.yml`
- Các file chứa khóa bảo mật (*.jks, *.p12)

#### 4.2. Quy tắc bảo mật
1. KHÔNG commit các file chứa thông tin nhạy cảm
2. Sử dụng biến môi trường cho các thông tin nhạy cảm
3. Mã hóa JWT secret key bằng Base64
4. Sử dụng HTTPS cho production
5. Đặt mật khẩu mạnh cho database và các dịch vụ

### 5. Kiểm tra hoạt động
1. Eureka Server: http://localhost:8761
2. Auth Service: http://localhost:8081
3. Post Service: http://localhost:8082
4. Media Service: http://localhost:8083

### 6. Xử lý sự cố
Nếu gặp vấn đề:
1. Kiểm tra logs:
```bash
docker-compose logs -f [service_name]
```

2. Kiểm tra kết nối database:
```bash
docker-compose exec mysql-auth mysql -u root -p
docker-compose exec mysql-post mysql -u root -p
```

3. Kiểm tra Redis:
```bash
docker-compose exec redis redis-cli ping
```

## Đóng góp
1. Fork repository
2. Tạo branch mới
3. Commit changes
4. Tạo pull request

## License
MIT License 