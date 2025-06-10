package com.expensetracker.backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // Lombok: Constructor với tất cả đối số
public class MessageResponse {
    private String message;
}