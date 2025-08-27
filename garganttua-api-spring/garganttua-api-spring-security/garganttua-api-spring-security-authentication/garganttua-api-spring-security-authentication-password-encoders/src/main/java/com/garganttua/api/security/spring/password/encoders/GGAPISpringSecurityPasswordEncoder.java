package com.garganttua.api.security.spring.password.encoders;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration	
public class SpringSecurityPasswordEncoder {

	@Bean
	public ISpringPasswordEncoder getEncoder() {
		return new SpringPasswordEncoderBcrypt( new BCryptPasswordEncoder());
	}
	
}
