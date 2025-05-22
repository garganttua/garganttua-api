package com.garganttua.api.interfaces.security.spring.rest;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

	private static final String DECODED_AUTHORIZATION = "decodedAuthorization";

	@Inject
	private IGGAPISecurityEngine security;

	@Inject
	private AuthenticationManager manager;

	@Override
	protected HttpServletRequest doFilter(HttpServletRequest request, HttpServletResponse response)
			throws GGAPIException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);

		if (caller.getAccess() == GGAPIServiceAccess.anonymous)
			return request;

		Optional<Object> authorization = this.security.getAuthorizationFromRequest(caller,
				request);

		authorization.ifPresentOrElse(auth -> {
			IGGAPIAuthenticationRequest authenticationRequest;
			try {
				authenticationRequest = this.security.createAuthenticationRequestFromAuthorization(caller, auth);
				Authentication springAuthentication = this.manager
						.authenticate(new GGAPISpringAuthenticationRequest(authenticationRequest));
				SecurityContextHolder.getContext().setAuthentication(springAuthentication);
	
				request.setAttribute(DECODED_AUTHORIZATION, authenticationRequest.getCredentials());
			} catch (GGAPIException e) {
				log.atDebug().log("errror while creating authentication request from authorization", e);
			}
		}, () -> log.atDebug().log("No authorization found"));

		return request;
	}
}
