package com.garganttua.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestFieldTypeChecks {
	
	@Test
	private void test2() {
		BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
		String passwordEncoded = "$2a$10$hF56Hj60PVc63rHb1A5tau8jTi5oJtOlE1O79EhddiiD4vuJ8S8.q";
		boolean result = bcrypt.matches("test", passwordEncoded);
		assertTrue(result);
	}
	
}
