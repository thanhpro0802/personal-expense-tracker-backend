package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.User;
import com.expensetracker.backend.model.Wallet;
import com.expensetracker.backend.payload.request.CreateWalletRequest;
import com.expensetracker.backend.payload.request.InviteMemberRequest;
import com.expensetracker.backend.repository.UserRepository;
import com.expensetracker.backend.security.jwt.AuthTokenFilter;
import com.expensetracker.backend.security.jwt.JwtUtils;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String authToken;
    private User testUser;
    private User secondUser;

    @BeforeEach
    public void setup() {
        // Create test users
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .passwordHash(passwordEncoder.encode("password123"))
                .build();
        testUser = userRepository.save(testUser);

        secondUser = User.builder()
                .username("seconduser")
                .email("second@example.com")
                .name("Second User")
                .passwordHash(passwordEncoder.encode("password123"))
                .build();
        secondUser = userRepository.save(secondUser);

        // Generate auth token for test user
        authToken = jwtUtils.generateTokenFromUserId(testUser.getId().toString());
    }

    @Test
    public void testCreateWallet() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("My Personal Wallet");
        request.setType(Wallet.WalletType.personal);

        mockMvc.perform(post("/api/wallets")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Personal Wallet"))
                .andExpect(jsonPath("$.type").value("personal"))
                .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    public void testGetUserWallets() throws Exception {
        // Create a wallet first
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("Test Wallet");
        request.setType(Wallet.WalletType.personal);

        mockMvc.perform(post("/api/wallets")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Get user wallets
        mockMvc.perform(get("/api/wallets")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Wallet"));
    }

    @Test
    public void testGetWalletDetail() throws Exception {
        // Create a wallet
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("Detail Wallet");
        request.setType(Wallet.WalletType.shared);

        MvcResult result = mockMvc.perform(post("/api/wallets")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String walletId = objectMapper.readTree(responseBody).get("id").asText();

        // Get wallet detail
        mockMvc.perform(get("/api/wallets/" + walletId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Detail Wallet"))
                .andExpect(jsonPath("$.type").value("shared"))
                .andExpect(jsonPath("$.members").isArray())
                .andExpect(jsonPath("$.members[0].username").value("testuser"))
                .andExpect(jsonPath("$.members[0].role").value("owner"));
    }

    @Test
    public void testInviteMemberToWallet() throws Exception {
        // Create a wallet
        CreateWalletRequest createRequest = new CreateWalletRequest();
        createRequest.setName("Shared Wallet");
        createRequest.setType(Wallet.WalletType.shared);

        MvcResult result = mockMvc.perform(post("/api/wallets")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String walletId = objectMapper.readTree(responseBody).get("id").asText();

        // Invite second user
        InviteMemberRequest inviteRequest = new InviteMemberRequest();
        inviteRequest.setUsername("seconduser");

        mockMvc.perform(post("/api/wallets/" + walletId + "/members")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("seconduser"))
                .andExpect(jsonPath("$.role").value("member"));
    }

    @Test
    public void testInviteMemberWithoutOwnerRole() throws Exception {
        // Create a wallet as testUser
        CreateWalletRequest createRequest = new CreateWalletRequest();
        createRequest.setName("Owner's Wallet");
        createRequest.setType(Wallet.WalletType.shared);

        MvcResult result = mockMvc.perform(post("/api/wallets")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String walletId = objectMapper.readTree(responseBody).get("id").asText();

        // Invite secondUser as a member
        InviteMemberRequest inviteRequest = new InviteMemberRequest();
        inviteRequest.setUsername("seconduser");

        mockMvc.perform(post("/api/wallets/" + walletId + "/members")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteRequest)));

        // Try to invite another user as secondUser (member, not owner)
        String secondUserToken = jwtUtils.generateTokenFromUserId(secondUser.getId().toString());
        
        InviteMemberRequest thirdUserInvite = new InviteMemberRequest();
        thirdUserInvite.setUsername("thirduser");

        mockMvc.perform(post("/api/wallets/" + walletId + "/members")
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(thirdUserInvite)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateWalletWithoutAuthentication() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("Unauthorized Wallet");
        request.setType(Wallet.WalletType.personal);

        mockMvc.perform(post("/api/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetWalletDetailNotMember() throws Exception {
        // Create a wallet as testUser
        CreateWalletRequest createRequest = new CreateWalletRequest();
        createRequest.setName("Private Wallet");
        createRequest.setType(Wallet.WalletType.personal);

        MvcResult result = mockMvc.perform(post("/api/wallets")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String walletId = objectMapper.readTree(responseBody).get("id").asText();

        // Try to access as secondUser (not a member)
        String secondUserToken = jwtUtils.generateTokenFromUserId(secondUser.getId().toString());

        mockMvc.perform(get("/api/wallets/" + walletId)
                .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isForbidden());
    }
}
