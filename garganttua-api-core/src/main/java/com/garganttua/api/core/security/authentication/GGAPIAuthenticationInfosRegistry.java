package com.garganttua.api.core.security.authentication;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInfosRegistry;

public class GGAPIAuthenticationInfosRegistry implements IGGAPIAuthenticationInfosRegistry {

	private Map<Class<?>, GGAPIAuthenticationInfos> authentications;

	public GGAPIAuthenticationInfosRegistry(Map<Class<?>, GGAPIAuthenticationInfos> authentications) {
		this.authentications = authentications;
	}

	@Override
	public List<GGAPIAuthenticationInfos> getAuthenticationInfos() {
		return List.copyOf(this.authentications.values());
	}

	@Override
	public List<Class<?>> getAuthentications() {
		return List.copyOf(this.authentications.keySet());
	}

	@Override
	public GGAPIAuthenticationInfos getAuthenticationInfos(Class<?> authenticationType) {
		return this.authentications.get(authenticationType);
	}

}
