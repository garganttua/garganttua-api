package com.garganttua.api.spec.security.authentication;

import java.util.Map;

public interface IAuthenticationFactoriesRegistry {

	IAuthenticationFactory getFactory(Class<?> authenticationType);

	Map<Class<?>, IAuthenticationFactory> getFactories();

}
