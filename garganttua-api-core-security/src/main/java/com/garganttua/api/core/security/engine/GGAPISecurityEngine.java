package com.garganttua.api.core.security.engine;

import java.util.Optional;
import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPIOwnerVerifier;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.security.IGGAPITenantVerifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPISecurityEngine implements IGGAPISecurityEngine {

	@Override
	public Optional<IGGAPIAuthenticationManager> getAuthenticationManager() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<IGGAPITenantVerifier> getTenantVerifier() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<IGGAPIOwnerVerifier> getOwnerVerifier() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<IGGAPIAuthorizationManager> getAuthorizationManager() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public void init(Set<IGGAPIDomain> domains) {
		log.info("Garganttua API Security Engin initalisation");
		
	}

}
