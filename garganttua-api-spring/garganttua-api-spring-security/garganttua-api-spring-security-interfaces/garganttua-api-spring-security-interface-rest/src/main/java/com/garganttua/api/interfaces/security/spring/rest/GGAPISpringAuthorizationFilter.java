package com.garganttua.api.interfaces.security.spring.rest;

import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.security.authentication.GGAPIAuthenticationRequest;
import com.garganttua.api.core.security.authentication.authorization.GGAPIAuthorizationAuthentication;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPISpringHttpApiFilter;
import com.garganttua.api.security.spring.core.authentication.GGAPISpringAuthenticationRequest;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringAuthorizationFilter extends GGAPISpringHttpApiFilter {
	
	@Inject 
	private IGGAPISecurityEngine security;
	
	@Inject 
	private AuthenticationManager manager;
	
	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);
		if( caller.getAccessRule().getAccess() != GGAPIServiceAccess.anonymous ) {
			byte[] authorizationRaw = this.security.decodeAuthorizationFromRequest(request, caller);
			if( authorizationRaw != null && authorizationRaw.length > 0 ) {
				Object authorization = this.security.decodeRawAuthorization(authorizationRaw, caller);
				if( authorization != null ) {
					IGGAPIAuthenticationRequest authenticationRequest = new GGAPIAuthenticationRequest(caller.getDomain(), caller.getTenantId(), GGAPIEntityAuthorizationHelper.getUuid(authorization), authorization, GGAPIAuthorizationAuthentication.class);						
					this.manager.authenticate(new GGAPISpringAuthenticationRequest(authenticationRequest));				
				} else {
					log.atWarn().log("Undecodable authorization "+new String(authorizationRaw));
				}
			} else {
				log.atWarn().log("Undecodable authorization");
			}
		}
	}
}
