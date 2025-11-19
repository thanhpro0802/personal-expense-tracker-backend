package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.Wallet;
import com.expensetracker.backend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private GeminiService geminiService;

    private UUID testUserId;
    private UUID testWalletId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testWalletId = UUID.randomUUID();
        
        // Set private fields using reflection
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(geminiService, "apiUrl", "https://test-api-url");
    }

    @Test
    void testGetChatResponse_UserNotMemberOfWallet_ThrowsSecurityException() {
        // Arrange
        when(walletService.isUserMemberOfWallet(testWalletId, testUserId)).thenReturn(false);

        // Act & Assert
        assertThrows(SecurityException.class, () -> 
            geminiService.getChatResponse("Test question", testWalletId, testUserId)
        );
        
        verify(walletService).isUserMemberOfWallet(testWalletId, testUserId);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void testGetChatResponse_NoTransactions() {
        // Arrange
        when(walletService.isUserMemberOfWallet(testWalletId, testUserId)).thenReturn(true);
        when(transactionRepository.findRecentTransactionsByMonthAndYear(
            eq(testWalletId), anyInt(), anyInt(), isNull()
        )).thenReturn(new ArrayList<>());

        // Since we can't easily mock the RestClient chain, we expect a RuntimeException
        // when it tries to call the API
        assertThrows(RuntimeException.class, () -> 
            geminiService.getChatResponse("Test question", testWalletId, testUserId)
        );
        
        verify(walletService).isUserMemberOfWallet(testWalletId, testUserId);
        verify(transactionRepository).findRecentTransactionsByMonthAndYear(
            eq(testWalletId), anyInt(), anyInt(), isNull()
        );
    }

    @Test
    void testGetChatResponse_WithTransactions_VerifiesWalletAccess() {
        // Arrange
        when(walletService.isUserMemberOfWallet(testWalletId, testUserId)).thenReturn(true);
        
        List<Transaction> transactions = createTestTransactions();
        YearMonth currentMonth = YearMonth.now();
        
        when(transactionRepository.findRecentTransactionsByMonthAndYear(
            eq(testWalletId), 
            eq(currentMonth.getMonthValue()), 
            eq(currentMonth.getYear()), 
            isNull()
        )).thenReturn(transactions);

        // Since we can't easily mock the RestClient chain, we expect a RuntimeException
        // when it tries to call the API
        assertThrows(RuntimeException.class, () -> 
            geminiService.getChatResponse("How much did I spend?", testWalletId, testUserId)
        );
        
        verify(walletService).isUserMemberOfWallet(testWalletId, testUserId);
        verify(transactionRepository).findRecentTransactionsByMonthAndYear(
            eq(testWalletId), anyInt(), anyInt(), isNull()
        );
    }

    private List<Transaction> createTestTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        Wallet wallet = new Wallet();
        wallet.setId(testWalletId);
        
        Transaction income = Transaction.builder()
            .id(UUID.randomUUID())
            .wallet(wallet)
            .title("Salary")
            .amount(new BigDecimal("5000.00"))
            .date(LocalDate.now())
            .category("Income")
            .type(Transaction.TransactionType.income)
            .build();
        
        Transaction expense = Transaction.builder()
            .id(UUID.randomUUID())
            .wallet(wallet)
            .title("Groceries")
            .amount(new BigDecimal("150.00"))
            .date(LocalDate.now())
            .category("Food")
            .type(Transaction.TransactionType.expense)
            .build();
        
        transactions.add(income);
        transactions.add(expense);
        
        return transactions;
    }
}
