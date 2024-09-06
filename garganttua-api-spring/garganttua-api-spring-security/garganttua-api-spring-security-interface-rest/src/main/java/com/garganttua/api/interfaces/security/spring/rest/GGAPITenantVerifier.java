package com.garganttua.api.interfaces.security.spring.rest;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class GGAPITenantVerifier extends OncePerRequestFilter {
	
	@Autowired
	private IGGAPISecurityEngine security;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		IGGAPIAuthorization authorization = (IGGAPIAuthorization) auth.getPrincipal();
		
		try {
			this.security.verifyTenant(caller, authorization);
		} catch (GGAPIException e) {
			throw new IOException(e);
		}
		
		filterChain.doFilter(request, response);
	}

}
