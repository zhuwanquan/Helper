package com.example.helper.Common.Util;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 成功响应 - 带数据
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    // 成功响应 - 不带数据
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(200, message, null);
    }
}
