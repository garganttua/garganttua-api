package com.garganttua.api.security.spring.core;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface ISpringSecurityRestConfigurer {

	HttpSecurity configureFilterChain(HttpSecurity http) throws Exception;

}
