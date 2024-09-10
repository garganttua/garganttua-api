package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface IGGAPISpringSecurityRestConfigurer {

	HttpSecurity configureFilterChain(HttpSecurity http) throws Exception;

}
