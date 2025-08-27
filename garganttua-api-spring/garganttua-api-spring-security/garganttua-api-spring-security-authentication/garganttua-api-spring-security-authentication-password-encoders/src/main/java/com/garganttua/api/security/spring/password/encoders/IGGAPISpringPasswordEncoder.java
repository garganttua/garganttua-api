package com.garganttua.api.security.spring.password.encoders;

import org.springframework.security.crypto.password.PasswordEncoder;

public interface ISpringPasswordEncoder extends PasswordEncoder {

	String encode(String password);

}
