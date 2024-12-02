package com.garganttua.api.security.spring.core.authentication;

import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationServicesRegistry;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class GGAPISpringAuthenticationProvider implements AuthenticationProvider {

	@Inject 
	private IGGAPISecurityEngine security;
	
	@Inject
	private IGGAPIEngine engine; 
	
	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {
		GGAPISpringAuthenticationRequest authentication = (GGAPISpringAuthenticationRequest) auth;
		IGGAPIAuthenticationRequest request = (IGGAPIAuthenticationRequest) authentication.getCredentials();
		IGGAPIAuthenticationServicesRegistry authenticationServicesRegistry = this.security.getAuthenticationServicesRegistry();
		IGGAPIAuthenticationService authenticationService = authenticationServicesRegistry.getService(request.getAuthenticationType());
		
		if( authenticationService != null ) {
			IGGAPIDomain domain = this.engine.getDomainsRegistry().getDomain(request.getDomain().getDomain());
			if( domain != null ) {
				IGGAPIServiceResponse response = authenticationService.authenticate(request);
				if( response.getResponseCode() == GGAPIServiceResponseCode.OK ) {
					return new GGAPISpringAuthentication(response.getResponse()) ;
				}
				log.atWarn().log("Authentication failed");
				throw new AuthenticationException("Authentication failed"){
					private static final long serialVersionUID = -1494230305460118934L;
				};
			} 
		}
		log.atWarn().log("Authentication failed");
		throw new AuthenticationException("Authentication failed"){
			private static final long serialVersionUID = -1494230305460118934L;
		};
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return GGAPISpringAuthenticationRequest.class.isAssignableFrom(authentication);
	}
}
