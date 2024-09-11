package com.garganttua.api.security.core;

import java.security.SecureRandom;

public class GGAPISecurityDefaultSecureRandom {

    public static final SecureRandom DEFAULT_SECURE_RANDOM;

    static {
        DEFAULT_SECURE_RANDOM = new SecureRandom();
        DEFAULT_SECURE_RANDOM.nextBytes(new byte[64]);
    }
	
}
