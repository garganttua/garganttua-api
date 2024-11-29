package com.garganttua.api.core.security.authentication;

import java.util.Map;

import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationServicesRegistry;

public class GGAPIAuthenticationServicesRegistry implements IGGAPIAuthenticationServicesRegistry {

	private Map<Class<?>, IGGAPIAuthenticationService> services;

	public GGAPIAuthenticationServicesRegistry(Map<Class<?>, IGGAPIAuthenticationService> services) {
		this.services = services;
	}

	@Override
	public IGGAPIAuthenticationService getService(Class<?> authentication) {
		return this.services.get(authentication);
	}

}
