# Blog Authentication Service

## Cấu hình

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

## Lưu ý quan trọng

1. **Bảo mật:**
   - Không commit file `application.yml` lên git
   - Sử dụng App Password cho Gmail thay vì mật khẩu thông thường
   - Đảm bảo JWT secret đủ mạnh và độc nhất
   - Trong môi trường production, nên sử dụng biến môi trường hoặc config server

2. **Môi trường:**
   - Development: `ddl-auto: update`
   - Production: `ddl-auto: none`
   - Test: `ddl-auto: create-drop`

3. **Logging:**
   - Development: Có thể bật `show-sql: true`
   - Production: Nên tắt `show-sql: false`

## Cách lấy App Password cho Gmail

1. Vào Google Account Settings
2. Security > 2-Step Verification
3. App passwords
4. Tạo mới một app password cho ứng dụng
5. Copy password và dán vào `application.yml`

## Cấu hình cho môi trường khác nhau

1. **Development:**
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application-dev.yml
   ```

2. **Production:**
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application-prod.yml
   ```

3. **Test:**
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application-test.yml
   ```

## Chạy ứng dụng với profile khác nhau

```bash
# Development
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Test
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
``` 