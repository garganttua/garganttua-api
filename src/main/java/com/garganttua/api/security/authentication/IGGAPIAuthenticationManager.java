package com.garganttua.api.security.authentication;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface IGGAPIAuthenticationManager {

	HttpSecurity configureFilterChain(HttpSecurity http) throws IGGAPISecurityException;

}
