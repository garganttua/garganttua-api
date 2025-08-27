package com.garganttua.api.spec.security.authentication;

public interface IAuthenticationServicesRegistry {

	IAuthenticationService getService(Class<?> authentication);

}
