package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Service;

@Service
public class GGAPISpringSecurityRestConfigurerDummy implements IGGAPISpringSecurityRestConfigurer {

	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) {
		return http;
	}

}
