package com.garganttua.api.core.security.authentication;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterfacesRegistry;

public class GGAPIAuthenticationInterfacesRegistry implements IGGAPIAuthenticationInterfacesRegistry {

	private Map<String, IGGAPIAuthenticationInterface> authenticationInterfaces;

	public GGAPIAuthenticationInterfacesRegistry(Map<String, IGGAPIAuthenticationInterface> authenticationInterfaces) {
		this.authenticationInterfaces = authenticationInterfaces;
	}

	@Override
	public List<IGGAPIAuthenticationInterface> getInterfaces() {
		return List.copyOf(this.authenticationInterfaces.values());
	}

}
