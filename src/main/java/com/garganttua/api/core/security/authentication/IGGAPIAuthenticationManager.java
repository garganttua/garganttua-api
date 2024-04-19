package com.garganttua.api.core.security.authentication;

import java.util.Optional;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.factory.GGAPIFactoryException;
import com.garganttua.api.core.security.GGAPISecurityException;

public interface IGGAPIAuthenticationManager {

	HttpSecurity configureFilterChain(HttpSecurity http) throws GGAPISecurityException;

	Optional<PasswordEncoder> getPasswordEncoder();

	Object applySecurityOnAuthenticatorEntity(Object entity) throws GGAPISecurityException;
	
	IGGAPIAuthenticator getAuthenticatorFromOwnerId(String tenantId, String ownerId) throws GGAPIEntityException, GGAPIFactoryException;
	
}
