package com.garganttua.api.security.spring.core;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

@Configuration
public class GGAPISpringSecurityConfiguration {
	
	@Autowired
	private Optional<IGGAPIAuthorizationManager> authorizationManager;
	
	@Autowired
	private Optional<IGGAPIAuthenticationManager> authenticationManager;

	@Autowired
	private IGGAPIEngine engine;
	
	@Bean  
	public IGGAPISecurityEngine securityEngine() {
		return this.engine.getSecurity();
	}

}
