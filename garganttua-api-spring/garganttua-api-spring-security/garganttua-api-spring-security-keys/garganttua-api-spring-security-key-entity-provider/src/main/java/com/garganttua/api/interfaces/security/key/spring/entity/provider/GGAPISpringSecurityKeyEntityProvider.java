package com.garganttua.api.interfaces.security.key.spring.entity.provider;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.security.key.GGAPIKeyManager;
import com.garganttua.api.core.security.key.GGAPIKeyRealm;
import com.garganttua.api.security.spring.core.keys.GGAPISpringSecurityKeyEntityRequest;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.IGGAPIService;

import jakarta.annotation.PostConstruct;

@Service
public class GGAPISpringSecurityKeyEntityProvider {


	@Value("${com.garganttua.api.security.keys.provider.lifetime}")
	private long keyLifetime;

	@Value("${com.garganttua.api.security.keys.provider.lifetime.unit}")
	private TimeUnit keyLifetimeUnit;
	
	@Value("${com.garganttua.api.security.keys.provider.autoCreate}")
	private boolean autoCreate = true;

	@Autowired
	private IGGAPIEngine engine;
	private IGGAPIService keyRealmService;
	private IGGAPIDomain domain;

	private GGAPIKeyManager manager;

	@PostConstruct
	private void init() {
		this.keyRealmService = this.engine.getServicesRegistry().getService(GGAPIKeyRealm.domain);
		this.domain = this.engine.getDomainsRegistry().getDomain(GGAPIKeyRealm.domain);
		
		this.manager = new GGAPIKeyManager(this.keyLifetime, this.keyLifetimeUnit, this.autoCreate, this.keyRealmService, this.domain);
	}
	
	public IGGAPIKeyRealm getRealm(IGGAPICaller caller, String realmUuid) throws GGAPIException {
		return this.manager.getRealm(caller, realmUuid);
	}
	
	public IGGAPIKeyRealm getRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException {
		return this.manager.getRealm(caller, request.keyRealmName(), request.algorithm());
	}

	public IGGAPIKeyRealm createRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException {
		return this.manager.createRealm(caller, request.keyRealmName(), request.algorithm());
	}

	public IGGAPIKeyRealm revokeRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException {
		return this.manager.revokeRealm(caller, request.keyRealmName(), request.algorithm());
	}

	public void revokeAllRealms(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException {
		this.manager.revokeAllRealms(caller);
	}

}
