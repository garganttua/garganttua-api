package com.garganttua.api.core.legacy.security.authentication;

import com.garganttua.api.spec.security.authentication.IAuthenticationService;
import com.garganttua.api.spec.security.authentication.IAuthenticationServicesRegistry;

public class AuthenticationServicesRegistry implements IAuthenticationServicesRegistry {

	private IAuthenticationService services;

	public AuthenticationServicesRegistry(IAuthenticationService services) {
		this.services = services;
	}

	@Override
	public IAuthenticationService getService(Class<?> authentication) {
		return this.services;
	}

}
