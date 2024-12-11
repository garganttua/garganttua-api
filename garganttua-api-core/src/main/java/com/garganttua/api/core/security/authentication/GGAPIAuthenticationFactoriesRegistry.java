package com.garganttua.api.core.security.authentication;

import java.util.Map;

import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactory;

public class GGAPIAuthenticationFactoriesRegistry implements IGGAPIAuthenticationFactoriesRegistry {

	private Map<Class<?>, IGGAPIAuthenticationFactory> factories;

	public GGAPIAuthenticationFactoriesRegistry(Map<Class<?>, IGGAPIAuthenticationFactory> factories) {
		this.factories = factories;
	}

	@Override
	public IGGAPIAuthenticationFactory getFactory(Class<?> authenticationRequestType) {
		return this.factories.get(authenticationRequestType);
	}

	@Override
	public Map<Class<?>, IGGAPIAuthenticationFactory> getFactories() {
		return this.factories;
	}

}
