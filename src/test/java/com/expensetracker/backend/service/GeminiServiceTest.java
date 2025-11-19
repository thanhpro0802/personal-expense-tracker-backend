package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AIChatRequest;
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
        List<AIChatRequest.HistoryMessage> history = null; // No history

        // When using Reflection to call private method
        String prompt = ReflectionTestUtils.invokeMethod(
                geminiService,
                "buildPrompt",
                testTransactions,
                question,
                history
        );

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("System: You are a helpful financial advisor"), "Should contain system instruction");
        assertTrue(prompt.contains("--- Transaction History ---"), "Should contain transaction section");
        assertTrue(prompt.contains("2023-10-01: -50.00$ (Food) - Lunch"));
        assertTrue(prompt.contains("2023-10-05: +3000.00$ (Income) - Salary"));
        assertTrue(prompt.contains("--- User Question ---"));
        assertTrue(prompt.contains(question));
    }

    @Test
    void testBuildPrompt_WithHistory_ShouldIncludeConversationHistory() {
        // Given
        String question = "Why is it so high?";

        List<AIChatRequest.HistoryMessage> history = new ArrayList<>();
        AIChatRequest.HistoryMessage msg1 = new AIChatRequest.HistoryMessage();
        msg1.setRole("user");
        msg1.setContent("Total expense?");
        history.add(msg1);

        AIChatRequest.HistoryMessage msg2 = new AIChatRequest.HistoryMessage();
        msg2.setRole("assistant");
        msg2.setContent("It is $500.");
        history.add(msg2);

        // When
        String prompt = ReflectionTestUtils.invokeMethod(
                geminiService,
                "buildPrompt",
                testTransactions,
                question,
                history
        );

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("--- Conversation History ---"), "Should contain history section");
        assertTrue(prompt.contains("User: Total expense?"), "Should contain user history");
        assertTrue(prompt.contains("AI: It is $500."), "Should contain AI history");
        assertTrue(prompt.contains("--- End History ---"));
        assertTrue(prompt.contains(question));
    }

    @Test
    void testBuildPrompt_WithEmptyTransactions_ShouldStillGeneratePrompt() {
        // Given
        List<Transaction> emptyList = new ArrayList<>();
        String question = "What is my balance?";
        List<AIChatRequest.HistoryMessage> history = new ArrayList<>();

        // When
        String prompt = ReflectionTestUtils.invokeMethod(
                geminiService,
                "buildPrompt",
                emptyList,
                question,
                history
        );

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("No recent transactions found."));
        assertTrue(prompt.contains(question));
    }

    @Test
    void testBuildPrompt_FormattingCheck() {
        // Given
        String question = "Check format";
        List<AIChatRequest.HistoryMessage> history = null;

        // When
        String prompt = ReflectionTestUtils.invokeMethod(
                geminiService,
                "buildPrompt",
                testTransactions,
                question,
                history
        );

        // Then
        // Check basic markdown formatting elements implicitly expected
        assertTrue(prompt.contains("-50.00$"), "Expense should have minus sign");
        assertTrue(prompt.contains("+3000.00$"), "Income should have plus sign");
    }
}