package com.garganttua.api.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.engine.accessors.IGGAPIAuthenticatorAccessor;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;

import jakarta.annotation.PostConstruct;

@Service(value = "authenticatorAccessor")
public class GGAPIAuthenticatorAccessor implements IGGAPIAuthenticatorAccessor {

	@Autowired
	private IGGAPIDynamicDomainsRegistry dynamicDomainsRegistry;
	
	@Autowired
	private IGGAPIControllersRegistry controllersRegistry;
	
	private Class<?> authenticator;
	
	private IGGAPIController<IGGAPIAuthenticator, IGGAPIDTOObject<IGGAPIAuthenticator>> controller;
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() {
		for( GGAPIDynamicDomain ddomain: this.dynamicDomainsRegistry.getDynamicDomains() ) {
			if( ddomain.authenticatorEntity() ) {
				this.authenticator = ddomain.entityClass();
				this.controller = (IGGAPIController<IGGAPIAuthenticator, IGGAPIDTOObject<IGGAPIAuthenticator>>) this.controllersRegistry.getController(ddomain.domain());
				break;
			}
		}
	}

	@Override
	public IGGAPIController<IGGAPIAuthenticator, IGGAPIDTOObject<IGGAPIAuthenticator>> getAuthenticatorController() {
		return this.controller;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IGGAPIAuthenticator> getAuthenticator() {
		return (Class<IGGAPIAuthenticator>) this.authenticator;
	}

}
