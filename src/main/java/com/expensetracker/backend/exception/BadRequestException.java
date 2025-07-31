package com.expensetracker.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception tùy chỉnh được ném ra khi một yêu cầu từ client chứa dữ liệu không hợp lệ
 * hoặc không thể xử lý được.
 * Việc sử dụng @ResponseStatus(HttpStatus.BAD_REQUEST) sẽ tự động khiến Spring Boot
 * trả về mã trạng thái HTTP 400 Bad Request khi exception này không được bắt ở đâu khác.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    /**
     * Constructor với một thông điệp lỗi.
     * @param message Thông điệp mô tả lỗi, ví dụ: "Error: Username is already taken!"
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructor với thông điệp lỗi và nguyên nhân gốc rễ.
     * @param message Thông điệp mô tả lỗi.
     * @param cause Nguyên nhân gốc rễ của exception.
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}