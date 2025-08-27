package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.spring.core.ISpringSecurityRestConfigurer;

@Service
public class SpringSecurityRestConfigurerDummy implements ISpringSecurityRestConfigurer {

	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) {
		return http;
	}

}
