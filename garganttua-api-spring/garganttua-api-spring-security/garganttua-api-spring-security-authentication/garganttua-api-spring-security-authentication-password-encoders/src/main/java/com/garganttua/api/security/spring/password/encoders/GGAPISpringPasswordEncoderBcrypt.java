package com.garganttua.api.security.spring.password.encoders;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SpringPasswordEncoderBcrypt implements ISpringPasswordEncoder {

	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public SpringPasswordEncoderBcrypt(BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return this.bCryptPasswordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return this.bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
	}

	@Override
	public String encode(String password) {
		return this.bCryptPasswordEncoder.encode(password);
	}

}
