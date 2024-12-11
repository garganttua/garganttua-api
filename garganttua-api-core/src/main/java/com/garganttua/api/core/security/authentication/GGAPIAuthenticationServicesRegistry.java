package com.garganttua.api.core.security.authentication;

import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationServicesRegistry;

public class GGAPIAuthenticationServicesRegistry implements IGGAPIAuthenticationServicesRegistry {

	private IGGAPIAuthenticationService services;

	public GGAPIAuthenticationServicesRegistry(IGGAPIAuthenticationService services) {
		this.services = services;
	}

	@Override
	public IGGAPIAuthenticationService getService(Class<?> authentication) {
		return this.services;
	}

}
