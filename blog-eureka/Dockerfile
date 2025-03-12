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

# Cài đặt curl cho health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Sao chép file JAR từ giai đoạn build
COPY --from=builder /app/blog-eureka/target/blog-eureka-*.jar blog-eureka.jar

# Biến môi trường
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8761
ENV EUREKA_HOST=localhost

# Expose port
EXPOSE 8761

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "blog-eureka.jar"]