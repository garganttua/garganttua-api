package com.garganttua.api.security.spring.authentication.loginpassword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.spring.password.encoders.IGGAPISpringPasswordEncoder;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringSecurityloginPasswordAuthenticationManager implements IGGAPIAuthenticationManager {
	
	@Autowired 
	private IGGAPISpringPasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationConfiguration configuration;

	@Override
	public Object applySecurityOnAuthenticatorEntity(Object entity) throws GGAPIException  {
		String password = null;
		try {
			password = GGAPILoginPasswordEntityAuthenticatorHelper.getPassword(entity);
		} catch (GGAPIException e) {
			log.warn("Password field not found for object of type "+entity.getClass().getSimpleName());
		}
		if( password != null && !password.isEmpty() ) {
			String encodedPassword = this.passwordEncoder.encode(password);
			GGAPILoginPasswordEntityAuthenticatorHelper.setPassword(entity, encodedPassword);
		}
		return entity;
	}

	@Override
	public IGGAPIAuthentication authenticate(IGGAPIAuthentication authentication) throws GGAPIException {

		GGAPILoginPasswordAuthentication authenticationRequest = (GGAPILoginPasswordAuthentication) authentication;
		try {
			Authentication authenticationReturned = this.configuration.getAuthenticationManager().authenticate(authenticationRequest);
			authenticationRequest.setAuthenticated(authenticationReturned);
			SecurityContextHolder.getContext().setAuthentication(authenticationRequest);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, e);
		}

		return authenticationRequest;
	}
}
