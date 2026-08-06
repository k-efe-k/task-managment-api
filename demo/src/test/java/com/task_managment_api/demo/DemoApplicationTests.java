package com.task_managment_api.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void checkPassword() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String hash = "$2a$10$ym6oshc7x2U4xtOwlArcyuzBuTnNuQWS2j3XKFxjFS7jLPWKLBSJ2";
		String[] candidates = {
			"password", "123456", "12345678", "password123", "123456789", 
			"test", "admin", "secret", "123", "qwert", "qwerty", "test123456", "test123"
		};
		for (String c : candidates) {
			if (encoder.matches(c, hash)) {
				System.out.println("FOUND_PASSWORD_MATCH: " + c);
				return;
			}
		}
		System.out.println("NO_PASSWORD_MATCH_FOUND");
	}

}
