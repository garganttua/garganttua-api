package com.garganttua.api.spec.security.authentication;

import java.util.Map;

public interface IGGAPIAuthenticationFactoriesRegistry {

	IGGAPIAuthenticationFactory getFactory(Class<?> authenticationType);

	Map<Class<?>, IGGAPIAuthenticationFactory> getFactories();

}
