package com.garganttua.api.security.spring.core.authentication;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IServiceResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class SpringAuthenticationProvider implements AuthenticationProvider {

	@Inject 
	private ISecurityEngine security;
	
	@Inject
	private IEngine engine; 
	
	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {
		SpringAuthenticationRequest authentication = (SpringAuthenticationRequest) auth;
		IAuthenticationRequest request = (IAuthenticationRequest) authentication.getCredentials();

		Optional<IDomain> domain = this.engine.getDomain(request.getDomain().getDomain());
		if( domain.isPresent() ) {
			IServiceResponse response = this.security.authenticate(request);
			if( response.getResponseCode() == ServiceResponseCode.OK ) {
				return new SpringAuthentication(response.getResponse()) ;
			}
			log.atWarn().log("Authentication failed");
			throw new AuthenticationException("Authentication failed"){
				private static final long serialVersionUID = -1494230305460118934L;
			};
		} 
			
		log.atWarn().log("Authentication failed");
		throw new AuthenticationException("Authentication failed"){
			private static final long serialVersionUID = -1494230305460118934L;
		};
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return SpringAuthenticationRequest.class.isAssignableFrom(authentication);
	}
}
