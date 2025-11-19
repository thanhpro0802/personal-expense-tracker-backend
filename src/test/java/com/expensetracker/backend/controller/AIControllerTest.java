package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.AIChatRequest;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIControllerTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AIController aiController;

    private UserDetailsImpl userDetails;
    private User testUser;
    private List<Transaction> testTransactions;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        userDetails = UserDetailsImpl.build(testUser);

        testTransactions = new ArrayList<>();
        Transaction txn = Transaction.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .title("Test Transaction")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .category("Test")
                .type(Transaction.TransactionType.expense)
                .build();
        testTransactions.add(txn);
    }

    @Test
    void testChat_WithValidRequest_ShouldReturnSuccessResponse() {
        // Given
        AIChatRequest request = new AIChatRequest("What is my total spending?");
        String expectedAIResponse = "Your total spending is $100.";

        when(transactionRepository.findRecentTransactionsByMonthAndYear(
                any(UUID.class), anyInt(), anyInt(), any(Pageable.class)))
                .thenReturn(testTransactions);
        when(geminiService.getAIResponse(anyList(), anyString()))
                .thenReturn(expectedAIResponse);

        // When
        ResponseEntity<?> response = aiController.chat(userDetails, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(transactionRepository, times(1))
                .findRecentTransactionsByMonthAndYear(any(UUID.class), anyInt(), anyInt(), any(Pageable.class));
        verify(geminiService, times(1))
                .getAIResponse(anyList(), anyString());
    }

    @Test
    void testChat_WithNullUserDetails_ShouldReturnUnauthorized() {
        // Given
        AIChatRequest request = new AIChatRequest("Test question");

        // When
        ResponseEntity<?> response = aiController.chat(null, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(transactionRepository, never())
                .findRecentTransactionsByMonthAndYear(any(), anyInt(), anyInt(), any());
        verify(geminiService, never()).getAIResponse(any(), any());
    }

    @Test
    void testChat_WithEmptyQuestion_ShouldReturnBadRequest() {
        // Given
        AIChatRequest request = new AIChatRequest("");

        // When
        ResponseEntity<?> response = aiController.chat(userDetails, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(transactionRepository, never())
                .findRecentTransactionsByMonthAndYear(any(), anyInt(), anyInt(), any());
        verify(geminiService, never()).getAIResponse(any(), any());
    }

    @Test
    void testChat_WithNullQuestion_ShouldReturnBadRequest() {
        // Given
        AIChatRequest request = new AIChatRequest(null);

        // When
        ResponseEntity<?> response = aiController.chat(userDetails, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(transactionRepository, never())
                .findRecentTransactionsByMonthAndYear(any(), anyInt(), anyInt(), any());
        verify(geminiService, never()).getAIResponse(any(), any());
    }

    @Test
    void testChat_ShouldRetrieveCurrentMonthTransactions() {
        // Given
        AIChatRequest request = new AIChatRequest("Test question");
        LocalDate now = LocalDate.now();
        int expectedMonth = now.getMonthValue();
        int expectedYear = now.getYear();

        when(transactionRepository.findRecentTransactionsByMonthAndYear(
                any(UUID.class), eq(expectedMonth), eq(expectedYear), any(Pageable.class)))
                .thenReturn(testTransactions);
        when(geminiService.getAIResponse(anyList(), anyString()))
                .thenReturn("AI Response");

        // When
        aiController.chat(userDetails, request);

        // Then
        verify(transactionRepository).findRecentTransactionsByMonthAndYear(
                any(UUID.class), eq(expectedMonth), eq(expectedYear), any(Pageable.class));
    }
}
