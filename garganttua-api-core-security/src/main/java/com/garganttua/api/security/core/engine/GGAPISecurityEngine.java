package com.garganttua.api.security.core.engine;

import java.util.List;
import java.util.Set;

import com.garganttua.api.security.core.accessRules.GGAPIAccessRulesRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPISecurityEngine implements IGGAPISecurityEngine {

	@Getter
	private GGAPIAccessRulesRegistry accessRulesRegistry;

//	@Override
//	public Optional<IGGAPIAuthenticationManager> getAuthenticationManager() {
//		// TODO Auto-generated method stub
//		return Optional.empty();
//	}
//
//	@Override
//	public Optional<IGGAPITenantVerifier> getTenantVerifier() {
//		// TODO Auto-generated method stub
//		return Optional.empty();
//	}
//
//	@Override
//	public Optional<IGGAPIOwnerVerifier> getOwnerVerifier() {
//		// TODO Auto-generated method stub
//		return Optional.empty();
//	}
//
//	@Override
//	public Optional<IGGAPIAuthorizationManager> getAuthorizationManager() {
//		// TODO Auto-generated method stub
//		return Optional.empty();
//	}

	@Override
	public void init(Set<IGGAPIDomain> domains) {
		log.info("Garganttua API Security Engine initalisation");
		this.accessRulesRegistry = new GGAPIAccessRulesRegistry(domains);
	}

	@Override
	public List<String> getAuthorities() {
		return this.accessRulesRegistry.getAuthorities();
	}

}
