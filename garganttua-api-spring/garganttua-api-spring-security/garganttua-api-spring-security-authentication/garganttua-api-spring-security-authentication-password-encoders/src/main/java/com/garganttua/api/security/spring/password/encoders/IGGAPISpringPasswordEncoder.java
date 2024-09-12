package com.garganttua.api.security.spring.password.encoders;

import org.springframework.security.crypto.password.PasswordEncoder;

public interface IGGAPISpringPasswordEncoder extends PasswordEncoder {

	String encode(String password);

}
