package com.blogApp.blogcommon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private T data;
    private boolean success;
    private int statusCode;
    private long timestamp;
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message, T data, HttpStatus status) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .success(true)
                .statusCode(status.value())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> error(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .message(message)
                .success(false)
                .statusCode(status.value())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, T data, HttpStatus status) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .success(false)
                .statusCode(status.value())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    // Các phương thức tiện ích cho các trường hợp phổ biến
    public static <T> ApiResponse<T> created(String message, T data) {
        return success(message, data, HttpStatus.CREATED);
    }
    
    public static <T> ApiResponse<T> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }
    
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(message, HttpStatus.UNAUTHORIZED);
    }
    
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(message, HttpStatus.FORBIDDEN);
    }
    
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }
} 