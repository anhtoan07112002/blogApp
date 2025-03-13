# PowerShell script for Blog App Docker

# Màu cho output
$Green = 'Green'
$Red = 'Red'
$Yellow = 'Yellow'
$White = 'White'

# Hiển thị banner
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor $Green
Write-Host "║                                                            ║" -ForegroundColor $Green
Write-Host "║                      BLOG APP DOCKER                       ║" -ForegroundColor $Green
Write-Host "║                                                            ║" -ForegroundColor $Green
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor $Green

# Kiểm tra Docker đã được cài đặt chưa
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Docker chưa được cài đặt. Vui lòng cài đặt Docker trước." -ForegroundColor $Red
    exit 1
}

# Kiểm tra Docker Compose đã được cài đặt chưa
try {
    docker compose version | Out-Null
}
catch {
    Write-Host "Docker Compose chưa được cài đặt hoặc không phải là một phần của Docker CLI. Vui lòng cài đặt Docker Compose trước." -ForegroundColor $Red
    exit 1
}

# Sửa lỗi cú pháp trong docker-compose.yml
function Fix-DockerComposeSyntax {
    Write-Host "Kiểm tra và sửa lỗi cú pháp trong docker-compose.yml..." -ForegroundColor $Yellow

    # Đọc nội dung file
    $content = Get-Content -Path "docker-compose.yml" -Raw

    # Sửa lỗi "driver: bridge twork" -> "driver: bridge"
    $content = $content -replace "driver: bridge twork", "driver: bridge"

    # Đảm bảo external: true -> external: false
    $content = $content -replace "external: true", "external: false"

    # Ghi lại nội dung file
    $content | Set-Content -Path "docker-compose.yml" -NoNewline

    Write-Host "Đã sửa cú pháp trong docker-compose.yml" -ForegroundColor $Green
}

# Kiểm tra và tạo file postgres-init.sql nếu chưa có
function Ensure-PostgresInitFile {
    if (-not (Test-Path -Path "./postgres-init.sql")) {
        Write-Host "Tạo file postgres-init.sql..." -ForegroundColor $Yellow

@"
-- Tạo database nếu chưa tồn tại
CREATE DATABASE blog_media_db;

-- Kết nối đến database vừa tạo
\c blog_media_db;

-- Tạo extension nếu cần
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tạo người dùng blog_media_user nếu chưa có
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'blog_media_user') THEN
    CREATE USER blog_media_user WITH PASSWORD 'Media@Secure123!';
  END IF;
END
\$\$;

-- Cấp quyền cho blog_media_user trên database blog_media_db
GRANT ALL PRIVILEGES ON DATABASE blog_media_db TO blog_media_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO blog_media_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO blog_media_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO blog_media_user;
ALTER SCHEMA public OWNER TO blog_media_user;
"@ | Set-Content -Path "./postgres-init.sql" -NoNewline

        Write-Host "Đã tạo file postgres-init.sql" -ForegroundColor $Green
    }
}

# Tạo Docker network nếu chưa tồn tại
$networkExists = $false
try {
    docker network inspect blog-network | Out-Null
    $networkExists = $true
}
catch {
    $networkExists = $false
}

if (-not $networkExists) {
    Write-Host "Tạo mạng Docker 'blog-network'..." -ForegroundColor $Yellow
    docker network create blog-network
}

# Kiểm tra file .env
function Check-EnvFile {
    if (-not (Test-Path -Path "./.env")) {
        Write-Host "Không tìm thấy file .env trong thư mục hiện tại." -ForegroundColor $Red
        Write-Host "Sử dụng file .env mặc định từ blog-media..." -ForegroundColor $Yellow

        # Nếu có file .env trong blog-media, sao chép ra thư mục gốc
        if (Test-Path -Path "./blog-media/.env") {
            Copy-Item -Path "./blog-media/.env" -Destination "./.env"

            # Cập nhật mật khẩu PostgreSQL
            $envContent = Get-Content -Path "./.env" -Raw
            $envContent = $envContent -replace "POSTGRES_PASSWORD=postgres", "POSTGRES_PASSWORD=StrongPostgresPassword!"
            $envContent | Set-Content -Path "./.env" -NoNewline

            Write-Host "Đã sao chép và điều chỉnh file .env từ blog-media." -ForegroundColor $Green
        }
        else {
            Write-Host "Không tìm thấy file .env trong blog-media." -ForegroundColor $Red
            Write-Host "Tạo file .env mặc định..." -ForegroundColor $Yellow

@"
# Cấu hình chung
SPRING_PROFILES_ACTIVE=prod

# Eureka
EUREKA_HOST=blog-eureka-server
EUREKA_SERVER=http://blog-eureka-server:8761/eureka/

# PostgreSQL Media
POSTGRES_USER=postgres
POSTGRES_PASSWORD=StrongPostgresPassword!
POSTGRES_DB=blog_media_db
SPRING_MEDIA_DATASOURCE_URL=jdbc:postgresql://postgres-media:5432/blog_media_db
SPRING_MEDIA_DATASOURCE_USERNAME=postgres
SPRING_MEDIA_DATASOURCE_PASSWORD=StrongPostgresPassword!

# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

# MinIO - Lưu trữ object
MINIO_ROOT_USER=minioAdmin
MINIO_ROOT_PASSWORD=Minio@Secure123!
MINIO_URL=http://minio:9000
MINIO_ACCESS_KEY=minioAdmin
MINIO_SECRET_KEY=Minio@Secure123!
MINIO_BUCKET_NAME=blog-media

# Service Ports
MEDIA_SERVER_PORT=8083

# Auth Service
AUTH_SERVICE_URL=http://blog-auth-service:8081

# JWT
JWT_SECRET=QURGREFERkFERkFCRERGQURGNTY3ODkwNDU2Nzg5MEFCQ0RFRg==

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080,*
"@ | Set-Content -Path "./.env" -NoNewline

            Write-Host "Đã tạo file .env mặc định" -ForegroundColor $Green
        }
    }
}

# Xem logs của một service
function View-Logs {
    Write-Host "Chọn service để xem logs:" -ForegroundColor $Yellow
    Write-Host "1) blog-eureka"
    Write-Host "2) blog-auth"
    Write-Host "3) blog-post"
    Write-Host "4) blog-media"
    Write-Host "5) mysql-auth"
    Write-Host "6) mysql-post"
    Write-Host "7) postgres-media"
    Write-Host "8) redis"
    Write-Host "9) minio"
    Write-Host "0) Quay lại"

    $serviceChoice = Read-Host "Nhập lựa chọn của bạn"

    switch ($serviceChoice) {
        1 { docker logs -f blog-eureka-server }
        2 { docker logs -f blog-auth-service }
        3 { docker logs -f blog-post-service }
        4 { docker logs -f blog-media-service }
        5 { docker logs -f blog-auth-mysql }
        6 { docker logs -f blog-post-mysql }
        7 { docker logs -f blog-postgres-media }
        8 { docker logs -f blog-redis }
        9 { docker logs -f blog-minio }
        0 { return }
        default { Write-Host "Lựa chọn không hợp lệ!" -ForegroundColor $Red }
    }
}

# Khởi động một service
function Start-SpecificService {
    Write-Host "Chọn service để khởi động:" -ForegroundColor $Yellow
    Write-Host "1) blog-eureka"
    Write-Host "2) blog-auth"
    Write-Host "3) blog-post"
    Write-Host "4) blog-media"
    Write-Host "5) mysql-auth"
    Write-Host "6) mysql-post"
    Write-Host "7) postgres-media"
    Write-Host "8) redis"
    Write-Host "9) minio"
    Write-Host "0) Quay lại"

    $serviceChoice = Read-Host "Nhập lựa chọn của bạn"

    switch ($serviceChoice) {
        1 { docker compose up -d blog-eureka }
        2 { docker compose up -d blog-auth }
        3 { docker compose up -d blog-post }
        4 { docker compose up -d blog-media }
        5 { docker compose up -d mysql-auth }
        6 { docker compose up -d mysql-post }
        7 { docker compose up -d postgres-media }
        8 { docker compose up -d redis }
        9 { docker compose up -d minio }
        0 { return }
        default { Write-Host "Lựa chọn không hợp lệ!" -ForegroundColor $Red }
    }
}

# Khởi động theo thứ tự tối ưu
function Start-ServicesOptimalOrder {
    # Sửa lỗi cú pháp
    Fix-DockerComposeSyntax

    # Đảm bảo có file postgres-init.sql
    Ensure-PostgresInitFile

    # Kiểm tra file .env
    Check-EnvFile

    Write-Host "Khởi động Eureka server..." -ForegroundColor $Yellow
    docker compose up -d blog-eureka
    Write-Host "Eureka server đã được khởi động!" -ForegroundColor $Green

    Write-Host "Đợi Eureka khởi động (30 giây)..." -ForegroundColor $Yellow
    Start-Sleep -Seconds 30

    Write-Host "Khởi động các database services..." -ForegroundColor $Yellow
    docker compose up -d mysql-auth mysql-post postgres-media redis minio
    Write-Host "Các database services đã được khởi động!" -ForegroundColor $Green

    Write-Host "Đợi database services khởi động (30 giây)..." -ForegroundColor $Yellow
    Start-Sleep -Seconds 30

    Write-Host "Khởi động blog-auth service..." -ForegroundColor $Yellow
    docker compose up -d blog-auth
    Write-Host "Blog-auth service đã được khởi động!" -ForegroundColor $Green

    Write-Host "Đợi blog-auth khởi động (15 giây)..." -ForegroundColor $Yellow
    Start-Sleep -Seconds 15

    Write-Host "Khởi động blog-post service..." -ForegroundColor $Yellow
    docker compose up -d blog-post
    Write-Host "Blog-post service đã được khởi động!" -ForegroundColor $Green

    Write-Host "Khởi động blog-media service..." -ForegroundColor $Yellow
    docker compose up -d blog-media
    Write-Host "Blog-media service đã được khởi động!" -ForegroundColor $Green

    Write-Host "Tất cả services đã được khởi động theo thứ tự tối ưu!" -ForegroundColor $Green
}

# Menu chức năng
function Show-Menu {
    Write-Host "Chọn một chức năng:" -ForegroundColor $Green
    Write-Host "1) Khởi động tất cả các service (theo thứ tự tối ưu)"
    Write-Host "2) Dừng tất cả các service"
    Write-Host "3) Khởi động lại tất cả các service"
    Write-Host "4) Xem trạng thái các service"
    Write-Host "5) Xem logs của một service cụ thể"
    Write-Host "6) Chỉ khởi động infrastructure services"
    Write-Host "7) Khởi động một service cụ thể"
    Write-Host "8) Xóa tất cả containers và volumes (cẩn thận!)"
    Write-Host "9) Rebuild và khởi động lại tất cả services"
    Write-Host "10) Sửa lỗi cú pháp trong docker-compose.yml"
    Write-Host "0) Thoát"
}

# Kiểm tra trước khi chạy script
Fix-DockerComposeSyntax
Ensure-PostgresInitFile
Check-EnvFile

# Xử lý menu chính
while ($true) {
    Show-Menu
    $choice = Read-Host "Nhập lựa chọn của bạn"

    switch ($choice) {
        1 {  # Khởi động tất cả services theo thứ tự tối ưu
            Start-ServicesOptimalOrder
        }
        2 {  # Dừng tất cả services
            Write-Host "Dừng tất cả services..." -ForegroundColor $Yellow
            docker compose down
            Write-Host "Tất cả services đã được dừng!" -ForegroundColor $Green
        }
        3 {  # Khởi động lại tất cả services
            Write-Host "Khởi động lại tất cả services..." -ForegroundColor $Yellow
            docker compose restart
            Write-Host "Tất cả services đã được khởi động lại!" -ForegroundColor $Green
        }
        4 {  # Xem trạng thái
            Write-Host "Trạng thái các services:" -ForegroundColor $Yellow
            docker compose ps
        }
        5 {  # Xem logs
            View-Logs
        }
        6 {  # Chỉ khởi động infrastructure services
            # Sửa lỗi cú pháp
            Fix-DockerComposeSyntax

            # Đảm bảo có file postgres-init.sql
            Ensure-PostgresInitFile

            Write-Host "Khởi động Eureka server..." -ForegroundColor $Yellow
            docker compose up -d blog-eureka

            Write-Host "Khởi động các database services..." -ForegroundColor $Yellow
            docker compose up -d mysql-auth mysql-post postgres-media redis minio
            Write-Host "Các infrastructure services đã được khởi động!" -ForegroundColor $Green
        }
        7 {  # Khởi động một service cụ thể
            Start-SpecificService
        }
        8 {  # Xóa tất cả containers và volumes
            Write-Host "CẢNH BÁO: Hành động này sẽ xóa tất cả containers và volumes!" -ForegroundColor $Red
            $confirm = Read-Host "Bạn có chắc chắn muốn tiếp tục? (y/N)"
            if ($confirm -eq "y" -or $confirm -eq "Y") {
                Write-Host "Đang xóa tất cả containers và volumes..." -ForegroundColor $Yellow
                docker compose down -v
                Write-Host "Đã xóa tất cả containers và volumes!" -ForegroundColor $Green
            }
            else {
                Write-Host "Đã hủy hành động!" -ForegroundColor $Yellow
            }
        }
        9 {  # Rebuild và khởi động lại
            Write-Host "Rebuild và khởi động lại tất cả services..." -ForegroundColor $Yellow
            docker compose down

            # Sửa lỗi cú pháp
            Fix-DockerComposeSyntax

            # Đảm bảo có file postgres-init.sql
            Ensure-PostgresInitFile

            docker compose build --no-cache

            # Khởi động theo thứ tự tối ưu
            Start-ServicesOptimalOrder
        }
        10 { # Sửa lỗi cú pháp trong docker-compose.yml
            Fix-DockerComposeSyntax
        }
        0 {  # Thoát
            Write-Host "Cảm ơn bạn đã sử dụng Docker Blog App!" -ForegroundColor $Green
            exit 0
        }
        default {  # Lựa chọn không hợp lệ
            Write-Host "Lựa chọn không hợp lệ!" -ForegroundColor $Red
        }
    }

    Write-Host ""
}