package com.expensetracker.backend.dto;

public class ApiResponse<T> {
    private boolean success;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

