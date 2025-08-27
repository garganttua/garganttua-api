package com.garganttua.api.spec.security.authentication;

import java.util.List;

public interface IAuthenticationInfosRegistry {

	List<AuthenticationInfos> getAuthenticationInfos();
	
	List<Class<?>> getAuthentications();

	AuthenticationInfos getAuthenticationInfos(Class<?> authenticationType);

}
