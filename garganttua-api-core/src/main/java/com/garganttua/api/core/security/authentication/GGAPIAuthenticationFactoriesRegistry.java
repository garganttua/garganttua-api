package com.garganttua.api.core.security.authentication;

import java.util.Map;

import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactory;

public class GGAPIAuthenticationFactoriesRegistry implements IGGAPIAuthenticationFactoriesRegistry {

	private Map<Class<?>, GGAPIAuthenticationFactory> factories;

	public GGAPIAuthenticationFactoriesRegistry(Map<Class<?>, GGAPIAuthenticationFactory> factories) {
		this.factories = factories;
	}

	@Override
	public IGGAPIAuthenticationFactory getFactory(Class<?> authenticationRequestType) {
		return this.factories.get(authenticationRequestType);
	}

}
