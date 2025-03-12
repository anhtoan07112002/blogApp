FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Sao chép toàn bộ mã nguồn
COPY . .

# Build ứng dụng
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Giai đoạn runtime
FROM openjdk:17-jdk-slim

WORKDIR /app

# Tạo thư mục cho dữ liệu Lucene index
RUN mkdir -p /app/data/index

# Sao chép file JAR từ giai đoạn build
COPY --from=builder /app/blog-post/target/blog-post-*.jar blog-post.jar

# Biến môi trường
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8082

# Expose port
EXPOSE 8082

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "blog-post.jar"] 