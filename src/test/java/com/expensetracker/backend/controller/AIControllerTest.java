package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.AIChatRequest;
import com.expensetracker.backend.dto.ApiResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
        AIChatRequest request = new AIChatRequest();
        request.setQuestion("What is my total spending?");
        request.setHistory(new ArrayList<>()); // Empty history

        String expectedAIResponse = "Your total spending is $100.";

        // Mock Page return for findByUser_Id
        Page<Transaction> transactionPage = new PageImpl<>(testTransactions);

        // Matcher cho Pageable: check page 0, size 100, sort DESC date
        when(transactionRepository.findByUser_Id(eq(testUser.getId()), any(Pageable.class)))
                .thenReturn(transactionPage);

        when(geminiService.getAIResponse(anyList(), anyString(), anyList()))
                .thenReturn(expectedAIResponse);

        // When
        ResponseEntity<?> response = aiController.chat(userDetails, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertTrue(apiResponse.isSuccess());

        // Verify Repository called with correct logic (100 items, sorted)
        verify(transactionRepository, times(1))
                .findByUser_Id(eq(testUser.getId()), argThat(pageable ->
                        pageable.getPageSize() == 100 &&
                                pageable.getSort().getOrderFor("date").getDirection() == Sort.Direction.DESC
                ));

        // Verify Service called with history
        verify(geminiService, times(1))
                .getAIResponse(eq(testTransactions), eq("What is my total spending?"), eq(request.getHistory()));
    }

    @Test
    void testChat_WithNullUserDetails_ShouldReturnUnauthorized() {
        // Given
        AIChatRequest request = new AIChatRequest();
        request.setQuestion("Test question");

        // When
        ResponseEntity<?> response = aiController.chat(null, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(transactionRepository, never()).findByUser_Id(any(), any());
        verify(geminiService, never()).getAIResponse(any(), any(), any());
    }

    @Test
    void testChat_WithEmptyQuestion_ShouldReturnBadRequest() {
        // Given
        AIChatRequest request = new AIChatRequest();
        request.setQuestion("");

        // When
        ResponseEntity<?> response = aiController.chat(userDetails, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(transactionRepository, never()).findByUser_Id(any(), any());
        verify(geminiService, never()).getAIResponse(any(), any(), any());
    }

    @Test
    void testChat_WithNullQuestion_ShouldReturnBadRequest() {
        // Given
        AIChatRequest request = new AIChatRequest();
        request.setQuestion(null);

        // When
        ResponseEntity<?> response = aiController.chat(userDetails, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(transactionRepository, never()).findByUser_Id(any(), any());
        verify(geminiService, never()).getAIResponse(any(), any(), any());
    }

    @Test
    void testChat_ShouldPassHistoryToService() {
        // Given
        AIChatRequest request = new AIChatRequest();
        request.setQuestion("Next question");

        List<AIChatRequest.HistoryMessage> history = new ArrayList<>();
        AIChatRequest.HistoryMessage msg = new AIChatRequest.HistoryMessage();
        msg.setRole("user");
        msg.setContent("Previous question");
        history.add(msg);
        request.setHistory(history);

        Page<Transaction> transactionPage = new PageImpl<>(testTransactions);
        when(transactionRepository.findByUser_Id(any(UUID.class), any(Pageable.class)))
                .thenReturn(transactionPage);
        when(geminiService.getAIResponse(anyList(), anyString(), anyList()))
                .thenReturn("Response");

        // When
        aiController.chat(userDetails, request);

        // Then
        verify(geminiService).getAIResponse(anyList(), eq("Next question"), eq(history));
    }
}