package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    private GeminiService geminiService;
    private User testUser;
    private List<Transaction> testTransactions;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService();
        
        // Set up test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        
        // Set up test transactions
        testTransactions = new ArrayList<>();
        
        Transaction txn1 = Transaction.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .title("Lunch")
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.of(2023, 10, 1))
                .category("Food")
                .type(Transaction.TransactionType.expense)
                .build();
        testTransactions.add(txn1);
        
        Transaction txn2 = Transaction.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .title("Salary")
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2023, 10, 5))
                .category("Income")
                .type(Transaction.TransactionType.income)
                .build();
        testTransactions.add(txn2);
    }

    @Test
    void testBuildPrompt_WithTransactions_ShouldGenerateCorrectPrompt() {
        // Given
        String question = "How much did I spend on food?";
        
        // When
        String prompt = geminiService.buildPrompt(testTransactions, question);
        
        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("You are a helpful financial advisor"));
        assertTrue(prompt.contains("Transaction History:"));
        assertTrue(prompt.contains("2023-10-01: -50.00$ (Food) - Lunch"));
        assertTrue(prompt.contains("2023-10-05: +3000.00$ (Income) - Salary"));
        assertTrue(prompt.contains("User Question: How much did I spend on food?"));
    }

    @Test
    void testBuildPrompt_WithEmptyTransactions_ShouldStillGeneratePrompt() {
        // Given
        List<Transaction> emptyList = new ArrayList<>();
        String question = "What is my balance?";
        
        // When
        String prompt = geminiService.buildPrompt(emptyList, question);
        
        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("You are a helpful financial advisor"));
        assertTrue(prompt.contains("Transaction History:"));
        assertTrue(prompt.contains("User Question: What is my balance?"));
    }

    @Test
    void testBuildPrompt_WithExpenseType_ShouldHaveMinusSign() {
        // Given
        String question = "Test question";
        
        // When
        String prompt = geminiService.buildPrompt(testTransactions, question);
        
        // Then
        assertTrue(prompt.contains("-50.00$"), "Expense should have minus sign");
    }

    @Test
    void testBuildPrompt_WithIncomeType_ShouldHavePlusSign() {
        // Given
        String question = "Test question";
        
        // When
        String prompt = geminiService.buildPrompt(testTransactions, question);
        
        // Then
        assertTrue(prompt.contains("+3000.00$"), "Income should have plus sign");
    }
}
