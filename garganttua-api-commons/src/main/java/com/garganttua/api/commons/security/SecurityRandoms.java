package com.garganttua.api.commons.security;

import java.security.SecureRandom;

public class SecurityRandoms {
	private static final SecureRandom DEFAULT_SECURE_RANDOM;

    static {
        DEFAULT_SECURE_RANDOM = new SecureRandom();
        DEFAULT_SECURE_RANDOM.nextBytes(new byte[64]);
    }

    private SecurityRandoms() {
    }

    public static SecureRandom secureRandom() {
        return DEFAULT_SECURE_RANDOM;
    }
}
