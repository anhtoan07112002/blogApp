# Hướng dẫn Docker cho Blog App

## Yêu cầu hệ thống

- Docker Engine (19.03.0+)
- Docker Compose (1.27.0+)
- Ít nhất 4GB RAM cho Docker (khuyến nghị 8GB)
- Khoảng 2GB dung lượng trống

## Demo chạy Docker

![Docker run demo](docs/images/demos/demoDocker.gif)

## Cấu trúc Docker

Blog App bao gồm các container sau:

1. **Service Discovery**: Eureka Server
2. **Cơ sở dữ liệu**: 
   - MySQL cho Auth Service
   - MySQL cho Post Service
   - PostgreSQL cho Media Service
3. **Cache**: Redis dùng chung
4. **Lưu trữ**: MinIO cho media files
5. **Services**:
   - Auth Service (JWT, xác thực)
   - Post Service (Quản lý bài viết)
   - Media Service (Quản lý media)

## Cách chạy ứng dụng

### 1. Chạy tự động với script

Script `start-docker.sh` (Linux/macOS) hoặc `start-docker.ps1` (Windows) cung cấp các chức năng:

- Tự động chuẩn bị mạng Docker và các tệp cấu hình
- Khởi động các dịch vụ theo thứ tự tối ưu
- Quản lý và xem logs
- Rebuild và khởi động lại services

### 2. Chạy thủ công với Docker Compose

```bash
# Tạo mạng Docker
docker network create blog-network

# Chạy theo thứ tự
docker-compose up -d blog-eureka
sleep 30  # Đợi Eureka khởi động

# Khởi động cơ sở dữ liệu
docker-compose up -d mysql-auth mysql-post postgres-media redis minio
sleep 30  # Đợi cơ sở dữ liệu khởi động

# Khởi động các services
docker-compose up -d blog-auth
sleep 15  # Đợi auth service khởi động
docker-compose up -d blog-post blog-media

# Xem trạng thái
docker-compose ps

# Xem logs
docker-compose logs -f [service-name]
```

## Build Docker Images

Nếu bạn cần tự build các Docker images:

```bash
# Đi đến thư mục gốc dự án
cd /path/to/blog

# Build image cho blog-auth
docker build -f blog-auth/Dockerfile -t blog-auth:latest .

# Build image cho blog-post
docker build -f blog-post/Dockerfile -t blog-post:latest .

# Build image cho blog-media
docker build -f blog-media/Dockerfile -t blog-media:latest .

# Hoặc build tất cả với Docker Compose
docker-compose build
```

## Các lưu ý quan trọng

1. **Cấu trúc Multi-module**: Dự án sử dụng cấu trúc Maven multi-module, Dockerfile đã được cấu hình để build theo đúng thứ tự.

2. **Thứ tự khởi động**: Luôn khởi động theo thứ tự:
   - Eureka Server → Cơ sở dữ liệu → Auth Service → Các service khác

3. **Bảo mật**: File `.env` chứa các biến môi trường với giá trị mặc định. Trong môi trường production, hãy thay đổi tất cả mật khẩu và khóa bí mật.

4. **Khắc phục sự cố**:
   - Kiểm tra logs: `docker-compose logs -f [service-name]`
   - Đảm bảo thứ tự khởi động đúng
   - Kiểm tra cấu hình mạng Docker
   - Đảm bảo các port không bị chiếm dụng

5. **Sao lưu dữ liệu**: Các volumes Docker được sử dụng để lưu dữ liệu. Cân nhắc sao lưu thường xuyên.