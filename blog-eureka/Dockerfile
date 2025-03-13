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

# Định nghĩa các ARG với giá trị mặc định
ARG PROFILE=prod
ARG PORT=8761
ARG EUREKA_HOST=blog-eureka-server

# Biến môi trường 
ENV SPRING_PROFILES_ACTIVE=${PROFILE}
ENV SERVER_PORT=${PORT}
ENV EUREKA_HOST=${EUREKA_HOST}

# Expose port
EXPOSE ${PORT}

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "blog-eureka.jar"]