package com.garganttua.api.core.security.authentication;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.security.authentication.IAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterfacesRegistry;

public class AuthenticationInterfacesRegistry implements IAuthenticationInterfacesRegistry {

	private Map<String, IAuthenticationInterface> authenticationInterfaces;

	public AuthenticationInterfacesRegistry(Map<String, IAuthenticationInterface> authenticationInterfaces) {
		this.authenticationInterfaces = authenticationInterfaces;
	}

	@Override
	public List<IAuthenticationInterface> getInterfaces() {
		return List.copyOf(this.authenticationInterfaces.values());
	}

}
