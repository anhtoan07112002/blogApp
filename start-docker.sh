#!/bin/bash

# Màu cho output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Hiển thị banner
echo -e "${GREEN}"
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                                                            ║"
echo "║                      BLOG APP DOCKER                       ║"
echo "║                                                            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Kiểm tra Docker đã được cài đặt chưa
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker chưa được cài đặt. Vui lòng cài đặt Docker trước.${NC}"
    exit 1
fi

# Kiểm tra Docker Compose đã được cài đặt chưa
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Docker Compose chưa được cài đặt. Vui lòng cài đặt Docker Compose trước.${NC}"
    exit 1
fi

# Sửa lỗi cú pháp trong docker-compose.yml
fix_docker_compose_syntax() {
    echo -e "${YELLOW}Kiểm tra và sửa lỗi cú pháp trong docker-compose.yml...${NC}"
    # Sửa lỗi "driver: bridge twork" -> "driver: bridge"
    sed -i 's/driver: bridge twork/driver: bridge/' docker-compose.yml
    # Đảm bảo external: true -> external: false
    sed -i 's/external: true/external: false/' docker-compose.yml
    echo -e "${GREEN}Đã sửa cú pháp trong docker-compose.yml${NC}"
}

# Kiểm tra và tạo file postgres-init.sql nếu chưa có
ensure_postgres_init_file() {
    if [ ! -f "./postgres-init.sql" ]; then
        echo -e "${YELLOW}Tạo file postgres-init.sql...${NC}"
        cat > ./postgres-init.sql << 'EOL'
-- Tạo database nếu chưa tồn tại
CREATE DATABASE blog_media_db;

-- Kết nối đến database vừa tạo
\c blog_media_db;

-- Tạo extension nếu cần
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tạo người dùng blog_media_user nếu chưa có
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'blog_media_user') THEN
    CREATE USER blog_media_user WITH PASSWORD 'Media@Secure123!';
  END IF;
END
$$;

-- Cấp quyền cho blog_media_user trên database blog_media_db
GRANT ALL PRIVILEGES ON DATABASE blog_media_db TO blog_media_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO blog_media_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO blog_media_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO blog_media_user;
ALTER SCHEMA public OWNER TO blog_media_user;
EOL
        echo -e "${GREEN}Đã tạo file postgres-init.sql${NC}"
    fi
}

# Tạo Docker network nếu chưa tồn tại
if ! docker network inspect blog-network &> /dev/null; then
    echo -e "${YELLOW}Tạo mạng Docker 'blog-network'...${NC}"
    docker network create blog-network
fi

# Kiểm tra file .env
check_env_file() {
    if [ ! -f "./.env" ]; then
        echo -e "${RED}Không tìm thấy file .env trong thư mục hiện tại.${NC}"
        echo -e "${YELLOW}Sử dụng file .env mặc định từ blog-media...${NC}"
        
        # Nếu có file .env trong blog-media, sao chép ra thư mục gốc
        if [ -f "./blog-media/.env" ]; then
            cp ./blog-media/.env ./.env
            # Cập nhật mật khẩu PostgreSQL
            sed -i 's/POSTGRES_PASSWORD=postgres/POSTGRES_PASSWORD=StrongPostgresPassword!/' ./.env
            echo -e "${GREEN}Đã sao chép và điều chỉnh file .env từ blog-media.${NC}"
        else
            echo -e "${RED}Không tìm thấy file .env trong blog-media.${NC}"
            echo -e "${YELLOW}Tạo file .env mặc định...${NC}"
            cat > ./.env << 'EOL'
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
EOL
            echo -e "${GREEN}Đã tạo file .env mặc định${NC}"
        fi
    fi
}

# Menu chức năng
show_menu() {
    echo -e "${GREEN}Chọn một chức năng:${NC}"
    echo "1) Khởi động tất cả các service (theo thứ tự tối ưu)"
    echo "2) Dừng tất cả các service"
    echo "3) Khởi động lại tất cả các service"
    echo "4) Xem trạng thái các service"
    echo "5) Xem logs của một service cụ thể"
    echo "6) Chỉ khởi động infrastructure services"
    echo "7) Khởi động một service cụ thể"
    echo "8) Xóa tất cả containers và volumes (cẩn thận!)"
    echo "9) Rebuild và khởi động lại tất cả services"
    echo "10) Sửa lỗi cú pháp trong docker-compose.yml"
    echo "0) Thoát"
}

# Xem logs của một service
view_logs() {
    echo -e "${YELLOW}Chọn service để xem logs:${NC}"
    echo "1) blog-eureka"
    echo "2) blog-auth"
    echo "3) blog-post"
    echo "4) blog-media"
    echo "5) mysql-auth"
    echo "6) mysql-post"
    echo "7) postgres-media"
    echo "8) redis"
    echo "9) minio"
    echo "0) Quay lại"
    
    read -p "Nhập lựa chọn của bạn: " service_choice
    
    case $service_choice in
        1) docker logs -f blog-eureka-server ;;
        2) docker logs -f blog-auth-service ;;
        3) docker logs -f blog-post-service ;;
        4) docker logs -f blog-media-service ;;
        5) docker logs -f blog-auth-mysql ;;
        6) docker logs -f blog-post-mysql ;;
        7) docker logs -f blog-postgres-media ;;
        8) docker logs -f blog-redis ;;
        9) docker logs -f blog-minio ;;
        0) return ;;
        *) echo -e "${RED}Lựa chọn không hợp lệ!${NC}" ;;
    esac
}

# Khởi động một service
start_specific_service() {
    echo -e "${YELLOW}Chọn service để khởi động:${NC}"
    echo "1) blog-eureka"
    echo "2) blog-auth"
    echo "3) blog-post"
    echo "4) blog-media"
    echo "5) mysql-auth"
    echo "6) mysql-post"
    echo "7) postgres-media"
    echo "8) redis"
    echo "9) minio"
    echo "0) Quay lại"
    
    read -p "Nhập lựa chọn của bạn: " service_choice
    
    case $service_choice in
        1) docker-compose up -d blog-eureka ;;
        2) docker-compose up -d blog-auth ;;
        3) docker-compose up -d blog-post ;;
        4) docker-compose up -d blog-media ;;
        5) docker-compose up -d mysql-auth ;;
        6) docker-compose up -d mysql-post ;;
        7) docker-compose up -d postgres-media ;;
        8) docker-compose up -d redis ;;
        9) docker-compose up -d minio ;;
        0) return ;;
        *) echo -e "${RED}Lựa chọn không hợp lệ!${NC}" ;;
    esac
}

# Khởi động theo thứ tự tối ưu
start_services_optimal_order() {
    # Sửa lỗi cú pháp
    fix_docker_compose_syntax
    
    # Đảm bảo có file postgres-init.sql
    ensure_postgres_init_file
    
    # Kiểm tra file .env
    check_env_file
    
    echo -e "${YELLOW}Khởi động Eureka server...${NC}"
    docker-compose up -d blog-eureka
    echo -e "${GREEN}Eureka server đã được khởi động!${NC}"
    
    echo -e "${YELLOW}Đợi Eureka khởi động (30 giây)...${NC}"
    sleep 30
    
    echo -e "${YELLOW}Khởi động các database services...${NC}"
    docker-compose up -d mysql-auth mysql-post postgres-media redis minio
    echo -e "${GREEN}Các database services đã được khởi động!${NC}"
    
    echo -e "${YELLOW}Đợi database services khởi động (30 giây)...${NC}"
    sleep 30
    
    echo -e "${YELLOW}Khởi động blog-auth service...${NC}"
    docker-compose up -d blog-auth
    echo -e "${GREEN}Blog-auth service đã được khởi động!${NC}"
    
    echo -e "${YELLOW}Đợi blog-auth khởi động (15 giây)...${NC}"
    sleep 15
    
    echo -e "${YELLOW}Khởi động blog-post service...${NC}"
    docker-compose up -d blog-post
    echo -e "${GREEN}Blog-post service đã được khởi động!${NC}"
    
    echo -e "${YELLOW}Khởi động blog-media service...${NC}"
    docker-compose up -d blog-media
    echo -e "${GREEN}Blog-media service đã được khởi động!${NC}"
    
    echo -e "${GREEN}Tất cả services đã được khởi động theo thứ tự tối ưu!${NC}"
}

# Kiểm tra trước khi chạy script
fix_docker_compose_syntax
ensure_postgres_init_file
check_env_file

# Xử lý menu chính
while true; do
    show_menu
    read -p "Nhập lựa chọn của bạn: " choice
    
    case $choice in
        1)  # Khởi động tất cả services theo thứ tự tối ưu
            start_services_optimal_order
            ;;
        2)  # Dừng tất cả services
            echo -e "${YELLOW}Dừng tất cả services...${NC}"
            docker-compose down
            echo -e "${GREEN}Tất cả services đã được dừng!${NC}"
            ;;
        3)  # Khởi động lại tất cả services
            echo -e "${YELLOW}Khởi động lại tất cả services...${NC}"
            docker-compose restart
            echo -e "${GREEN}Tất cả services đã được khởi động lại!${NC}"
            ;;
        4)  # Xem trạng thái
            echo -e "${YELLOW}Trạng thái các services:${NC}"
            docker-compose ps
            ;;
        5)  # Xem logs
            view_logs
            ;;
        6)  # Chỉ khởi động infrastructure services
            # Sửa lỗi cú pháp
            fix_docker_compose_syntax
            
            # Đảm bảo có file postgres-init.sql
            ensure_postgres_init_file
            
            echo -e "${YELLOW}Khởi động Eureka server...${NC}"
            docker-compose up -d blog-eureka
            
            echo -e "${YELLOW}Khởi động các database services...${NC}"
            docker-compose up -d mysql-auth mysql-post postgres-media redis minio
            echo -e "${GREEN}Các infrastructure services đã được khởi động!${NC}"
            ;;
        7)  # Khởi động một service cụ thể
            start_specific_service
            ;;
        8)  # Xóa tất cả containers và volumes
            echo -e "${RED}CẢNH BÁO: Hành động này sẽ xóa tất cả containers và volumes!${NC}"
            read -p "Bạn có chắc chắn muốn tiếp tục? (y/N): " confirm
            if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
                echo -e "${YELLOW}Đang xóa tất cả containers và volumes...${NC}"
                docker-compose down -v
                echo -e "${GREEN}Đã xóa tất cả containers và volumes!${NC}"
            else
                echo -e "${YELLOW}Đã hủy hành động!${NC}"
            fi
            ;;
        9)  # Rebuild và khởi động lại
            echo -e "${YELLOW}Rebuild và khởi động lại tất cả services...${NC}"
            docker-compose down
            
            # Sửa lỗi cú pháp
            fix_docker_compose_syntax
            
            # Đảm bảo có file postgres-init.sql
            ensure_postgres_init_file
            
            docker-compose build --no-cache
            
            # Khởi động theo thứ tự tối ưu
            start_services_optimal_order
            ;;
        10) # Sửa lỗi cú pháp trong docker-compose.yml
            fix_docker_compose_syntax
            ;;
        0)  # Thoát
            echo -e "${GREEN}Cảm ơn bạn đã sử dụng Docker Blog App!${NC}"
            exit 0
            ;;
        *)  # Lựa chọn không hợp lệ
            echo -e "${RED}Lựa chọn không hợp lệ!${NC}"
            ;;
    esac
    
    echo ""
done 