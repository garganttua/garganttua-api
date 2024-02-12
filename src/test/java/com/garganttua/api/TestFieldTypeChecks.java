package com.garganttua.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.garganttua.api.security.GGAPISecurityException;
import com.garganttua.api.security.authentication.GGAPIAuthenticationManager;
import com.garganttua.api.security.authentication.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.security.authentication.modes.loginpassword.GGAPIAuthenticatorLogin;

public class TestFieldTypeChecks {
	
	private class TestClass {
		
		@GGAPIAuthenticatorLogin
		private String stringField;
		
		@GGAPIAuthenticatorAuthorities
		private List<String> listOfStringField;
	}

	@Test
	public void test() throws GGAPISecurityException {
		
		
		String fieldName = GGAPIAuthenticationManager.checkAnnotationIsPresent(TestClass.class, GGAPIAuthenticatorLogin.class, String.class);
		String fieldName2 = GGAPIAuthenticationManager.checkAnnotationIsPresent(TestClass.class, GGAPIAuthenticatorAuthorities.class, GGAPIAuthenticationManager.getListStringType());
		
		
		assertEquals("stringField", fieldName);
		assertEquals("listOfStringField", fieldName2);
		
	}
	
	@Test
	private void test2() {
		BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
		String passwordEncoded = "$2a$10$hF56Hj60PVc63rHb1A5tau8jTi5oJtOlE1O79EhddiiD4vuJ8S8.q";
		boolean result = bcrypt.matches("test", passwordEncoded);
		assertTrue(result);
	}
	
}
