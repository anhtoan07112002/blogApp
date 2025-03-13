# Giai đoạn build: Sử dụng image Maven chính thức
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy toàn bộ dự án vào container
COPY . .

# Build parent POM trước
WORKDIR /app
RUN mvn clean install -N

# Build blog-common sau
WORKDIR /app/blog-common
RUN mvn clean install -DskipTests

# Cuối cùng build blog-media
WORKDIR /app/blog-media
RUN mvn clean package -DskipTests

# Giai đoạn runtime: Sử dụng image JRE nhẹ hơn
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Sao chép file JAR từ giai đoạn build
COPY --from=builder /app/blog-media/target/blog-media-*.jar blog-media.jar

# Định nghĩa các ARG với giá trị mặc định
ARG PROFILE=prod
ARG PORT=8083
ARG DB_URL=jdbc:postgresql://postgres-media:5432/blog_media_db
ARG DB_USERNAME=postgres
ARG DB_PASSWORD=StrongPostgresPassword!
ARG REDIS_HOST=redis
ARG REDIS_PORT=6379
ARG MINIO_URL=http://minio:9000
ARG MINIO_ACCESS_KEY=minioAdmin
ARG MINIO_SECRET_KEY=Minio@Secure123!
ARG MINIO_BUCKET=blog-media
ARG AUTH_SERVICE_URL=http://blog-auth-service:8081
ARG EUREKA_URL=http://blog-eureka-server:8761/eureka/

# Biến môi trường
ENV SPRING_PROFILES_ACTIVE=${PROFILE}
ENV SERVER_PORT=${PORT}
ENV SPRING_DATASOURCE_URL=${DB_URL}
ENV SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
ENV SPRING_REDIS_HOST=${REDIS_HOST}
ENV SPRING_REDIS_PORT=${REDIS_PORT}
ENV MINIO_URL=${MINIO_URL}
ENV MINIO_ACCESS-KEY=${MINIO_ACCESS_KEY}
ENV MINIO_SECRET-KEY=${MINIO_SECRET_KEY}
ENV MINIO_BUCKET-NAME=${MINIO_BUCKET}
ENV APP_AUTH-SERVICE_URL=${AUTH_SERVICE_URL}
ENV EUREKA_CLIENT_SERVICE-URL_DEFAULTZONE=${EUREKA_URL}

# Expose port
EXPOSE ${PORT}

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "blog-media.jar"] 