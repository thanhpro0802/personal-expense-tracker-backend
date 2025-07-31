package com.expensetracker.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception tùy chỉnh được ném ra khi một tài nguyên cụ thể không được tìm thấy trong cơ sở dữ liệu.
 * Việc sử dụng @ResponseStatus(HttpStatus.NOT_FOUND) sẽ tự động khiến Spring Boot
 * trả về mã trạng thái HTTP 404 Not Found khi exception này không được bắt ở đâu khác.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor với một thông điệp lỗi.
     * @param message Thông điệp mô tả lỗi, ví dụ: "User not found with id: 123"
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor với thông điệp lỗi và nguyên nhân gốc rễ.
     * @param message Thông điệp mô tả lỗi.
     * @param cause Nguyên nhân gốc rễ của exception.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}