package com.garganttua.api.spec.security.authentication;

public interface IGGAPIAuthenticationServicesRegistry {

	IGGAPIAuthenticationService getService(Class<?> authentication);

}
