package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.beans.factory.annotation.Autowired;

import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPISpringHttpApiFilter;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManagerIfPresentMethod;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class GGAPISpringWithAuthorizationManagerGenericFilter extends GGAPISpringHttpApiFilter implements IGGAPIAuthorizationManagerIfPresentMethod {
	
	@Autowired 
	protected IGGAPISecurityEngine security;
	
	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);
		this.security.ifAuthorizationManagerPresent(this, caller);
	}
}
