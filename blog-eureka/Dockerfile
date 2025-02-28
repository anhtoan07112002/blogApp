FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Sao chép file JAR từ target vào container
COPY target/blog-eureka-0.0.1-SNAPSHOT.jar blog-eureka.jar

# Command để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "blog-eureka.jar"]