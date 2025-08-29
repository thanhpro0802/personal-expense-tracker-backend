package com.expensetracker.backend.security;

import com.expensetracker.backend.model.User;
import com.expensetracker.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        // H2 in-memory
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        // JWT properties (bắt buộc cho JwtUtils)
        "jwt.secret=THIS_IS_A_TEST_SECRET_32CHARS_MIN_LENGTH_1234567890",
        "jwt.access.expiration.ms=900000",
        "jwt.refresh.expiration.ms=604800000",
        // CORS để match frontend
        "server.port=0"
})
class AuthSigninSecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    private static class TestUser {
        String username;
        String password;
        String email;
        String name;

        TestUser(String username) {
            this.username = username;
            this.password = "password123";
            this.email = username + "@example.com";
            this.name = "Tester " + username;
        }
    }

    private User createUser(TestUser tu) {
        User u = new User();
        u.setId(null);
        u.setUsername(tu.username);
        u.setEmail(tu.email);
        u.setName(tu.name);
        // Lưu ý: UserService sẽ mã hóa password nếu PasswordEncoder có sẵn
        u.setPasswordHash(tu.password);
        return userService.createUser(u);
    }

    @BeforeEach
    void setup() {
        // Không cần seed chung; mỗi test sẽ tạo user riêng để tránh đụng unique constraints
    }

    // Test 1: Kỳ vọng đăng nhập thành công (200). Nếu nhận 403 => lỗi bảo mật (CSRF/CORS/permitAll)
    @Test
    void signin_shouldReturn200_withValidCredentials_andAllowedOrigin() throws Exception {
        TestUser tu = new TestUser("alice_" + UUID.randomUUID());
        createUser(tu);

        String body = objectMapper.writeValueAsString(Map.of(
                "username", tu.username,
                "password", tu.password
        ));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Tùy cấu trúc response, kiểm tra các field phổ biến
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(tu.username));
    }

    // Test 2: Nếu CSRF đang bật, POST không kèm CSRF sẽ bị 403. Với API JWT stateless, ta mong không cần CSRF.
    @Test
    void signin_shouldSucceed_withoutCsrf_forStatelessApi() throws Exception {
        TestUser tu = new TestUser("bob_" + UUID.randomUUID());
        createUser(tu);

        String body = objectMapper.writeValueAsString(Map.of(
                "username", tu.username,
                "password", tu.password
        ));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }

    // Test 3: Preflight CORS cho /api/auth/signin phải cho phép Origin http://localhost:5173
    @Test
    void cors_preflight_shouldAllowFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/signin")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        // Status có thể 200 hoặc 204 tùy cấu hình; quan trọng là header cho phép origin.
    }

    // Test 4: Origin sai (ví dụ 3000) không được phép -> thường 403 hoặc thiếu CORS headers
    @Test
    void cors_preflight_shouldRejectWrongOrigin() throws Exception {
        var result = mockMvc.perform(options("/api/auth/signin")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andReturn();

        int status = result.getResponse().getStatus();
        // Hợp lệ nếu 403 hoặc 200/204 nhưng KHÔNG có Access-Control-Allow-Origin header
        String allowOrigin = result.getResponse().getHeader("Access-Control-Allow-Origin");
        assertThat(allowOrigin).as("Wrong origin must not be allowed").isNull();
        assertThat(status).isIn(200, 204, 403);
    }

    // Test 5: Payload phải đúng (Content-Type + trường username/password). Nếu sai -> 4xx (không phải 403 do security)
    @Test
    void signin_shouldReturn4xx_whenPayloadInvalid_not403Security() throws Exception {
        String badBody = objectMapper.writeValueAsString(Map.of(
                "user", "missing_expected_field",
                "pass", "wrong_field_name"
        ));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().is4xxClientError()); // 400/422 tùy validator
    }
}