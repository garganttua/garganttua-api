package com.garganttua.api.security.spring.core.authentication;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Service;

import com.garganttua.api.spec.security.IGGAPISecurityEngine;

@Service
public class GGAPISpringAuthenticationManager {

	@Inject
	private IGGAPISecurityEngine security;
	
	@Autowired
	private List<AuthenticationProvider> authenticationProviders = new ArrayList<AuthenticationProvider>();

	@Bean
	public AuthenticationManager authenticationManager() {
		ProviderManager providerManager = new ProviderManager(this.authenticationProviders);
		
		this.security.getAuthenticationInterfacesRegistry().getInterfaces().forEach(authenticatioInterface -> {
			if( IGGAPISpringAuthenticationInterface.class.isAssignableFrom(authenticatioInterface.getClass()) ) {
				((IGGAPISpringAuthenticationInterface) authenticatioInterface).setAuthenticationManager(providerManager);
			}
		});

		return providerManager;
	}
}
