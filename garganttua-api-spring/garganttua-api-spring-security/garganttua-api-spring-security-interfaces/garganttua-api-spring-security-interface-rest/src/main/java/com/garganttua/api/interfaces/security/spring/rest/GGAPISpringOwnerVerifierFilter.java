package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPISpringHttpApiFilter;
import com.garganttua.api.security.spring.core.authentication.GGAPISpringAuthentication;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringOwnerVerifierFilter extends GGAPISpringHttpApiFilter {
	
	@Autowired
	private IGGAPISecurityEngine security;
	
	@Override
	protected HttpServletRequest doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); 
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);
		
		if( GGAPISpringAuthentication.class.isAssignableFrom(authentication.getClass()) ) {
			log.atDebug().log("Checking caller ownerId ["+caller.getOwnerId()+"] against authentication ownerId ["+((GGAPISpringAuthentication) authentication).getOwnerId()+"]");
			
			this.security.verifyOwner(caller, ((GGAPISpringAuthentication) authentication).getAuthentication());
		}
		return request;
	}
}
