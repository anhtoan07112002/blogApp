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

# Sao chép file JAR từ giai đoạn build
COPY --from=builder /app/blog-auth/target/blog-auth-*.jar blog-auth.jar

# Biến môi trường
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8081

# Expose port
EXPOSE 8081

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "blog-auth.jar"] 