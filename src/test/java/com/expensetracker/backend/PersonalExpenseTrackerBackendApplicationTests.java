package com.expensetracker.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
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
		"server.port=0",
		// Thêm giá trị giả cho gemini api key để Spring không báo lỗi thiếu placeholder
		"gemini.api.key=dummy-test-key"
})
class PersonalExpenseTrackerBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
