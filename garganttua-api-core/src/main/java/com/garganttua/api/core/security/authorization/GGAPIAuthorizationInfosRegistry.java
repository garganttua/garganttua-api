package com.garganttua.api.core.security.authorization;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationInfosRegistry;

public class GGAPIAuthorizationInfosRegistry implements IGGAPIAuthorizationInfosRegistry {

	private Map<Class<?>, GGAPIAuthorizationInfos> authorizations;

	public GGAPIAuthorizationInfosRegistry(Map<Class<?>, GGAPIAuthorizationInfos> authorizations) {
		this.authorizations = authorizations;
	}

	@Override
	public List<Class<?>> getAuthorizationsTypes() {
		return List.copyOf(this.authorizations.keySet());
	}

	@Override
	public List<GGAPIAuthorizationInfos> getAuthorizationsInfos() {
		return List.copyOf(this.authorizations.values());
	}

}
