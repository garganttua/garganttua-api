package com.garganttua.api.security.spring.password.encoders;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GGAPISpringPasswordEncoderBcrypt implements IGGAPISpringPasswordEncoder {

	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public GGAPISpringPasswordEncoderBcrypt(BCryptPasswordEncoder bCryptPasswordEncoder) {
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
