package com.garganttua.api.spec.security.authentication;

public interface IGGAPIAuthenticationFactoriesRegistry {

	IGGAPIAuthenticationFactory getFactory(Class<?> authenticationType);

}
