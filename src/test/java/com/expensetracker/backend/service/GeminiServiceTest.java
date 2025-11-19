package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "gemini.api.key=test-api-key",
    "gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
})
public class GeminiServiceTest {

    private GeminiService geminiService;

    @BeforeEach
    public void setup() {
        geminiService = new GeminiService();
    }

    @Test
    public void testBuildTransactionSummaryWithEmptyList() {
        List<Transaction> transactions = new ArrayList<>();
        
        // Use reflection to access private method for testing
        try {
            java.lang.reflect.Method method = GeminiService.class.getDeclaredMethod("buildTransactionSummary", List.class);
            method.setAccessible(true);
            String summary = (String) method.invoke(geminiService, transactions);
            
            assertEquals("No transactions found for the current month.", summary);
        } catch (Exception e) {
            fail("Failed to test buildTransactionSummary: " + e.getMessage());
        }
    }

    @Test
    public void testBuildTransactionSummaryWithTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        Transaction transaction1 = Transaction.builder()
                .title("Groceries")
                .amount(new BigDecimal("50.00"))
                .type(Transaction.TransactionType.expense)
                .category("Food")
                .date(LocalDate.now())
                .build();
        
        transactions.add(transaction1);
        
        try {
            java.lang.reflect.Method method = GeminiService.class.getDeclaredMethod("buildTransactionSummary", List.class);
            method.setAccessible(true);
            String summary = (String) method.invoke(geminiService, transactions);
            
            assertNotNull(summary);
            assertTrue(summary.contains("Transaction Summary:"));
            assertTrue(summary.contains("EXPENSE"));
            assertTrue(summary.contains("50.00"));
            assertTrue(summary.contains("Groceries"));
            assertTrue(summary.contains("Food"));
        } catch (Exception e) {
            fail("Failed to test buildTransactionSummary: " + e.getMessage());
        }
    }

    @Test
    public void testBuildPrompt() {
        String userQuestion = "How much did I spend this month?";
        String transactionSummary = "Transaction Summary:\n- EXPENSE: 50.00 Groceries (Category: Food, Date: 2023-11-19)";
        
        try {
            java.lang.reflect.Method method = GeminiService.class.getDeclaredMethod("buildPrompt", String.class, String.class);
            method.setAccessible(true);
            String prompt = (String) method.invoke(geminiService, userQuestion, transactionSummary);
            
            assertNotNull(prompt);
            assertTrue(prompt.contains(userQuestion));
            assertTrue(prompt.contains(transactionSummary));
            assertTrue(prompt.contains("User Question:"));
            assertTrue(prompt.contains("Context:"));
        } catch (Exception e) {
            fail("Failed to test buildPrompt: " + e.getMessage());
        }
    }
}
