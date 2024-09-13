package com.garganttua.api.security.authorizations.spring.jwt;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TestJwts {
	
	@Test
	public void testJwts() {
		
		List<String> algos = List.of("HS256",
		"HS384",
		"HS512",
		"RS256",
		"RS384",
		"RS512",
		"PS256",
		"PS384",
		"PS512",
		"ES256",
		"ES384",
		"ES512");
			
		
	}

}
