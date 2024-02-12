package com.garganttua.api.engine.accessors.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.accessors.IGGAPIAuthenticatorAccessor;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
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
	
	private IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> controller;
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() {
		for( GGAPIDynamicDomain ddomain: this.dynamicDomainsRegistry.getDynamicDomains() ) {
			if( ddomain.authenticatorEntity() ) {
				this.authenticator = ddomain.entityClass();
				this.controller = (IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) this.controllersRegistry.getController(ddomain.domain());
				break;
			}
		}
	}

	@Override
	public IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> getAuthenticatorController() {
		return this.controller;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IGGAPIEntity> getAuthenticator() {
		return (Class<IGGAPIEntity>) this.authenticator;
	}

}
