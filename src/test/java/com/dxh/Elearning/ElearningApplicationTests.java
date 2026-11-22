package com.dxh.Elearning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ElearningApplicationTests {
	@Autowired
	Environment environment;

	@Test
	void contextLoads() {
	}

	@Test
	void localVerificationLinkUsesConfiguredBackendPort() {
		assertEquals(
				"http://localhost:8081/api/users/confirm-email",
				environment.getProperty("spring.sendGrid.verificationLink")
		);
	}

}
