package com.garganttua.api.interfaces.security.spring.rest;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.interfaces.spring.rest.CallerFilter;
import com.garganttua.api.interfaces.spring.rest.SpringHttpApiFilter;
import com.garganttua.api.security.spring.core.authentication.SpringAuthenticationRequest;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.service.ServiceAccess;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SpringAuthorizationFilter extends SpringHttpApiFilter {

	private static final String DECODED_AUTHORIZATION = "decodedAuthorization";

	@Inject
	private ISecurityEngine security;

	@Inject
	private AuthenticationManager manager;

	@Override
	protected HttpServletRequest doFilter(HttpServletRequest request, HttpServletResponse response)
			throws CoreException {
		ICaller caller = (ICaller) request.getAttribute(CallerFilter.CALLER_ATTRIBUTE_NAME);

		if (caller.getAccess() == ServiceAccess.anonymous)
			return request;

		Optional<Object> authorization = this.security.getAuthorizationFromRequest(caller,
				request);

		authorization.ifPresentOrElse(auth -> {
			IAuthenticationRequest authenticationRequest;
			try {
				authenticationRequest = this.security.createAuthenticationRequestFromAuthorization(caller, auth);
				Authentication springAuthentication = this.manager
						.authenticate(new SpringAuthenticationRequest(authenticationRequest));
				SecurityContextHolder.getContext().setAuthentication(springAuthentication);
	
				request.setAttribute(DECODED_AUTHORIZATION, authenticationRequest.getCredentials());
				caller.setCallerId(EntityHelper.getUuidFromOwnerId(EntityAuthorizationHelper.getOwnerId(auth)));
			} catch (CoreException e) {
				log.atDebug().log("errror while creating authentication request from authorization", e);
			}
		}, () -> log.atDebug().log("No authorization found"));

		return request;
	}
}
