package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPISpringHttpApiFilter;
import com.garganttua.api.security.spring.core.authentication.IGGAPISpringAuthentication;
import com.garganttua.api.security.spring.core.authorizations.GGAPISpringSecurityAuthorizationProtocol;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class GGAPISpringAuthorizationFilter extends GGAPISpringHttpApiFilter {
	
	@Autowired
	private IGGAPISecurityEngine security;
	
	@Autowired 
	private GGAPISpringSecurityAuthorizationProtocol authorizationProtocol;

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);

		byte[] authorization = this.authorizationProtocol.getAuthorization(request);

		this.security.ifAuthorizationManagerPresent((method, caller_) -> {
			IGGAPISpringAuthentication authentication = (IGGAPISpringAuthentication) method.validateAuthorization(authorization);
			if( authentication == null )
				return ;
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}, caller);
	}
}
