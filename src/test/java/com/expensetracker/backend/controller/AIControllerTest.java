package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.ChatRequest;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeminiService geminiService;

    private UUID testUserId;
    private UUID testWalletId;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testWalletId = UUID.randomUUID();
        
        User testUser = User.builder()
            .id(testUserId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("password")
            .name("Test User")
            .build();
        
        UserDetailsImpl userDetails = new UserDetailsImpl(
            testUserId,
            "testuser",
            "test@example.com",
            "password",
            testUser,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    }

    @Test
    void testChatEndpoint_Success() throws Exception {
        // Arrange
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setQuestion("How much did I spend this month?");
        chatRequest.setWalletId(testWalletId);
        
        String mockResponse = "Based on your transactions, you spent $500 this month.";
        when(geminiService.getChatResponse(anyString(), any(UUID.class), any(UUID.class)))
            .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/ai/chat")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(mockResponse));
    }

    @Test
    void testChatEndpoint_SecurityException() throws Exception {
        // Arrange
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setQuestion("How much did I spend?");
        chatRequest.setWalletId(testWalletId);
        
        when(geminiService.getChatResponse(anyString(), any(UUID.class), any(UUID.class)))
            .thenThrow(new SecurityException("User is not a member of this wallet"));

        // Act & Assert
        mockMvc.perform(post("/api/ai/chat")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Access denied: User is not a member of this wallet"));
    }

    @Test
    void testChatEndpoint_GenericException() throws Exception {
        // Arrange
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setQuestion("What's my budget?");
        chatRequest.setWalletId(testWalletId);
        
        when(geminiService.getChatResponse(anyString(), any(UUID.class), any(UUID.class)))
            .thenThrow(new RuntimeException("API error"));

        // Act & Assert
        mockMvc.perform(post("/api/ai/chat")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Error: API error"));
    }

    @Test
    @WithMockUser
    void testChatEndpoint_WithoutAuthentication() throws Exception {
        // Arrange
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setQuestion("Test question");
        chatRequest.setWalletId(testWalletId);

        // Act & Assert
        mockMvc.perform(post("/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
