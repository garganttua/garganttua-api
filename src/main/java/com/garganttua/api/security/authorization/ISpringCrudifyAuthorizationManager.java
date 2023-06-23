package com.garganttua.api.security.authorization;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.garganttua.api.security.authentication.ISpringCrudifySecurityException;

public interface ISpringCrudifyAuthorizationManager {

	HttpSecurity configureFilterChain(HttpSecurity http) throws ISpringCrudifySecurityException;

}
