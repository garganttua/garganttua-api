package com.garganttua.api.core.legacy.security.authentication;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.security.authentication.AuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IAuthenticationInfosRegistry;

public class AuthenticationInfosRegistry implements IAuthenticationInfosRegistry {

	private Map<Class<?>, AuthenticationInfos> authentications;

	public AuthenticationInfosRegistry(Map<Class<?>, AuthenticationInfos> authentications) {
		this.authentications = authentications;
	}

	@Override
	public List<AuthenticationInfos> getAuthenticationInfos() {
		return List.copyOf(this.authentications.values());
	}

	@Override
	public List<Class<?>> getAuthentications() {
		return List.copyOf(this.authentications.keySet());
	}

	@Override
	public AuthenticationInfos getAuthenticationInfos(Class<?> authenticationType) {
		return this.authentications.get(authenticationType);
	}

}
