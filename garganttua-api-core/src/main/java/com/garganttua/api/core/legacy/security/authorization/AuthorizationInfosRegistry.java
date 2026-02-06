package com.garganttua.api.core.legacy.security.authorization;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.security.authorization.AuthorizationInfos;
import com.garganttua.api.spec.security.authorization.IAuthorizationInfosRegistry;

public class AuthorizationInfosRegistry implements IAuthorizationInfosRegistry {

	private Map<Class<?>, AuthorizationInfos> authorizations;

	public AuthorizationInfosRegistry(Map<Class<?>, AuthorizationInfos> authorizations) {
		this.authorizations = authorizations;
	}

	@Override
	public List<Class<?>> getAuthorizationsTypes() {
		return List.copyOf(this.authorizations.keySet());
	}

	@Override
	public List<AuthorizationInfos> getAuthorizationsInfos() {
		return List.copyOf(this.authorizations.values());
	}

}
