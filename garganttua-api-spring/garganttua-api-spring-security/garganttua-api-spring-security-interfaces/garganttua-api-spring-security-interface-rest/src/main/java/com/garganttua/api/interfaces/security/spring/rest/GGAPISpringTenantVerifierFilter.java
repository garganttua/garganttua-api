package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;

@Service
public class GGAPISpringTenantVerifierFilter extends GGAPISpringWithAuthorizationManagerGenericFilter {
	
	@Override
	public void ifPresent(IGGAPIAuthorizationManager manager, IGGAPICaller caller) throws GGAPIException {
		IGGAPIAuthentication auth = (IGGAPIAuthentication) SecurityContextHolder.getContext().getAuthentication();
		IGGAPIAuthorization authorization = auth.getAuthorization();
		this.security.verifyTenant(caller, authorization);
	}
}
