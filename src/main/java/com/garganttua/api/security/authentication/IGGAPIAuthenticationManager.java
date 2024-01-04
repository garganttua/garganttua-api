package com.garganttua.api.security.authentication;

import java.util.Optional;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.garganttua.api.security.GGAPISecurityException;

public interface IGGAPIAuthenticationManager {

	HttpSecurity configureFilterChain(HttpSecurity http) throws GGAPISecurityException;

	Optional<PasswordEncoder> getPasswordEncoder();

}
