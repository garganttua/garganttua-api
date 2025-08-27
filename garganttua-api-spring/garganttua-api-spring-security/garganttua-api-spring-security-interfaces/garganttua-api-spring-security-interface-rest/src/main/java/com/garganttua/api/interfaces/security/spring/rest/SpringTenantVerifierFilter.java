package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.interfaces.spring.rest.CallerFilter;
import com.garganttua.api.interfaces.spring.rest.SpringHttpApiFilter;
import com.garganttua.api.security.spring.core.authentication.SpringAuthentication;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.security.ISecurityEngine;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SpringTenantVerifierFilter extends SpringHttpApiFilter {
	
	@Autowired
	private ISecurityEngine security;
	
	@Override
	protected HttpServletRequest doFilter(HttpServletRequest request, HttpServletResponse response) throws CoreException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); 
		ICaller caller = (ICaller) request.getAttribute(CallerFilter.CALLER_ATTRIBUTE_NAME);
		
		if( SpringAuthentication.class.isAssignableFrom(authentication.getClass()) ) {
			log.atDebug().log("Checking caller tenantId ["+caller.getTenantId()+"] against authentication tenantId ["+((SpringAuthentication) authentication).getTenantId()+"]");
			
			this.security.verifyTenant(caller, ((SpringAuthentication) authentication).getAuthentication());
		}
		
		return request;
	}
}
