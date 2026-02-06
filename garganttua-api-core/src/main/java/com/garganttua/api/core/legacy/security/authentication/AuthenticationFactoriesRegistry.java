package com.garganttua.api.core.legacy.security.authentication;

import java.util.Map;

import com.garganttua.api.spec.security.authentication.IAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationFactory;

public class AuthenticationFactoriesRegistry implements IAuthenticationFactoriesRegistry {

	private Map<Class<?>, IAuthenticationFactory> factories;

	public AuthenticationFactoriesRegistry(Map<Class<?>, IAuthenticationFactory> factories) {
		this.factories = factories;
	}

	@Override
	public IAuthenticationFactory getFactory(Class<?> authenticationRequestType) {
		return this.factories.get(authenticationRequestType);
	}

	@Override
	public Map<Class<?>, IAuthenticationFactory> getFactories() {
		return this.factories;
	}

}
