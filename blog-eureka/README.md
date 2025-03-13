# Blog Eureka Service Discovery

Blog Eureka Service là trung tâm Service Discovery cho hệ sinh thái Blog Application microservices. Module này sử dụng Netflix Eureka để cung cấp khả năng đăng ký, khám phá và quản lý các dịch vụ trong hệ thống phân tán.

## Tính năng chính

- **Đăng ký dịch vụ**: Cho phép các service đăng ký chính mình với Eureka Server khi khởi động
- **Khám phá dịch vụ**: Cho phép các service tìm và giao tiếp với nhau mà không cần địa chỉ cứng
- **Tự phục hồi**: Tự động phát hiện dịch vụ khi chúng ngừng hoạt động hoặc khởi động lại
- **Cân bằng tải**: Hỗ trợ cân bằng tải phía client cho các microservice
- **Bảng điều khiển trực quan**: Cung cấp giao diện web để giám sát các dịch vụ đã đăng ký
- **Phân nhóm dịch vụ**: Hỗ trợ phân nhóm các dịch vụ theo vùng và môi trường

## Công nghệ sử dụng

- **Spring Boot**: Framework chính
- **Spring Cloud Netflix Eureka Server**: Triển khai Eureka Service Registry
- **Spring Boot Actuator**: Giám sát và quản lý dịch vụ
- **Spring Cloud Config**: (Tùy chọn) Tích hợp với Configuration Server

## Mô hình hoạt động

Eureka Server hoạt động theo mô hình client-server:

1. **Eureka Server** lưu giữ thông tin về tất cả các dịch vụ đã đăng ký trong bộ nhớ in-memory.
2. **Eureka Client** (các service khác) đăng ký với Eureka Server khi khởi động và gửi heartbeat định kỳ.
3. **Client Registry**: Các client tải xuống registry của server và lưu vào bộ nhớ cache cục bộ.
4. **Self-Preservation**: Nếu Eureka không nhận đủ heartbeat, nó sẽ kích hoạt chế độ tự bảo toàn.
5. **Service Discovery**: Các service sử dụng thông tin từ registry để giao tiếp với nhau.

## Cài đặt và Cấu hình

### 1. Chuẩn bị môi trường

- Java 17+
- RAM: ít nhất 512MB (khuyến nghị 1GB)
- Cổng mạng: 8761 (mặc định) phải được mở

### 2. Cấu hình

Cấu hình được lưu trữ trong file `application.yml`. Copy file cấu hình mẫu và điều chỉnh theo nhu cầu:

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Cấu hình cơ bản:

```yaml
server:
  port: 8761

spring:
  application:
    name: blog-eureka

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: true
    renewal-percent-threshold: 0.85
```

### 3. Cấu hình Nâng cao

#### Bảo mật Eureka với HTTPS và Xác thực

```yaml
server:
  port: 8761
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: eureka

spring:
  security:
    user:
      name: ${EUREKA_USERNAME}
      password: ${EUREKA_PASSWORD}

eureka:
  client:
    service-url:
      defaultZone: https://${spring.security.user.name}:${spring.security.user.password}@localhost:8761/eureka/
```

#### Cấu hình cho Môi trường Phân tán (Peer-to-Peer)

Trong môi trường production, nên triển khai nhiều Eureka Server để đảm bảo tính khả dụng cao:

**Eureka Server 1 (application-peer1.yml)**:
```yaml
spring:
  application:
    name: blog-eureka
  profiles: peer1

server:
  port: 8761

eureka:
  instance:
    hostname: peer1
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://peer2:8762/eureka/
```

**Eureka Server 2 (application-peer2.yml)**:
```yaml
spring:
  application:
    name: blog-eureka
  profiles: peer2

server:
  port: 8762

eureka:
  instance:
    hostname: peer2
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://peer1:8761/eureka/
```

### 4. Chạy ứng dụng

#### Chạy trực tiếp

```bash
# Khởi động với profile mặc định
./mvnw spring-boot:run

# Khởi động với profile cụ thể
./mvnw spring-boot:run -Dspring-boot.run.profiles=peer1
```

#### Chạy với Docker

```bash
# Xây dựng image
docker build -t blog-eureka .

# Chạy container
docker run -d -p 8761:8761 --name blog-eureka blog-eureka
```

#### Chạy với Docker Compose

```bash
# Khởi động dịch vụ
docker-compose up -d blog-eureka
```

## Sử dụng Eureka trong các Microservice

### 1. Cấu hình Client

Thêm các dependency sau vào `pom.xml` của các service khác:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### 2. Cấu hình application.yml cho Client

```yaml
spring:
  application:
    name: blog-service-name

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
```

### 3. Kích hoạt Eureka Client

Thêm annotation `@EnableDiscoveryClient` vào main application class:

```java
@SpringBootApplication
@EnableDiscoveryClient
public class BlogServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogServiceApplication.class, args);
    }
}
```

## Giám sát và Quản lý

### Bảng điều khiển Eureka

Truy cập bảng điều khiển Eureka thông qua trình duyệt:
```
http://localhost:8761
```

Bảng điều khiển cung cấp thông tin về:
- Danh sách các dịch vụ đã đăng ký
- Trạng thái của Eureka Server
- Môi trường và vùng
- Thời gian hoạt động

### Spring Boot Actuator

Các endpoint giám sát được cung cấp qua Spring Boot Actuator:

```
http://localhost:8761/actuator/health
http://localhost:8761/actuator/info
http://localhost:8761/actuator/metrics
```

## Xử lý sự cố

### Eureka Server không khởi động

1. Kiểm tra xung đột cổng:
```bash
netstat -ano | findstr 8761
```

2. Kiểm tra log lỗi:
```bash
cat logs/eureka.log
```

3. Đảm bảo Java 17+ đã được cài đặt:
```bash
java -version
```

### Self-Preservation Mode

Nếu bạn thấy cảnh báo về "EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT":

1. Đây là tính năng tự bảo toàn để ngăn Eureka xóa các dịch vụ khi có vấn đề mạng
2. Trong môi trường phát triển, bạn có thể tắt nó:
```yaml
eureka:
  server:
    enable-self-preservation: false
```

### Các dịch vụ không đăng ký

1. Kiểm tra cấu hình client:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
```

2. Kiểm tra logs của service:
```bash
cat logs/application.log | grep -i eureka
```

3. Đảm bảo Eureka Server đã khởi động trước các service khác

## Triển khai trong Môi trường Sản xuất

Khi triển khai Eureka Server trong môi trường sản xuất, hãy lưu ý những điểm sau:

1. **Sử dụng nhiều instance**: Triển khai ít nhất 2 Eureka Server trong cụm peer-to-peer
2. **Bảo mật**: Bật HTTPS và xác thực
3. **Đặt cụm trong VPC/mạng riêng**: Hạn chế truy cập từ bên ngoài
4. **Giám sát**: Cấu hình cảnh báo cho trạng thái Eureka Server
5. **Kích hoạt Logging**: Cấu hình ghi log đầy đủ để dễ dàng khắc phục sự cố
6. **Quản lý tài nguyên**: Cân nhắc giới hạn RAM/CPU phù hợp

## Tích hợp với Công cụ DevOps

Eureka Server có thể được tích hợp với các công cụ DevOps phổ biến:

1. **Prometheus & Grafana**: Giám sát và trực quan hóa trạng thái Eureka
2. **ELK Stack**: Tập trung và phân tích logs
3. **Kubernetes**: Quản lý triển khai và khả năng mở rộng
4. **Jenkins/GitLab CI**: Tự động hóa triển khai 