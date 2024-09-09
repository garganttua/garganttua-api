package com.garganttua.api.security.spring.core;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.garganttua.api.security.core.engine.GGAPIOwnerVerifier;
import com.garganttua.api.security.core.engine.GGAPISecurityBuilder;
import com.garganttua.api.security.core.engine.GGAPITenantVerifier;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

@Configuration
@EnableWebSecurity
public class GGAPISpringSecurityConfiguration {
	
	@Autowired
	private Optional<IGGAPIAuthorizationManager> authorizationManager;
	
	@Autowired
	private Optional<IGGAPIAuthenticationManager> authenticationManager;

	@Autowired
	private IGGAPIEngine engine;
	
	@Bean
	public IGGAPISecurityEngine createSecurityEngine() {
		GGAPISecurityBuilder builder = new GGAPISecurityBuilder();
		this.authenticationManager.ifPresent(authenticationManager -> {builder.authenticationManager(authenticationManager);});
		this.authorizationManager.ifPresent(authorizationManager -> {builder.authorizationManager(authorizationManager);});
		
		builder.domains(engine.getDomainsRegistry().getDomains());
		builder.ownerVerifier(new GGAPIOwnerVerifier());
		builder.tenantVerifier(new GGAPITenantVerifier());
		
		return builder.build();
	}
}
