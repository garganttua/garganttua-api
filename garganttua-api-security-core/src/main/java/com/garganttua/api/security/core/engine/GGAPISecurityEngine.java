package com.garganttua.api.security.core.engine;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.security.core.accessRules.GGAPIAccessRulesRegistry;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPIOwnerVerifier;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.security.IGGAPITenantVerifier;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPISecurityEngine implements IGGAPISecurityEngine {

	@Getter
	protected GGAPIAccessRulesRegistry accessRulesRegistry;
	protected Optional<IGGAPIDomain> authenticatorDomain;
	
	protected Optional<IGGAPIAuthenticationManager> authenticationManager; 
	protected Optional<IGGAPIAuthorizationManager> authorizationManager;
	protected Optional<IGGAPITenantVerifier> tenantVerifier;
	protected Optional<IGGAPIOwnerVerifier> ownerVerifier;
	
	protected Set<IGGAPIDomain> domains; 

	public GGAPISecurityEngine(Optional<IGGAPIAuthorizationManager> authorizationManager, Optional<IGGAPIAuthenticationManager> authenticationManager, Optional<IGGAPITenantVerifier> tenantVerifier, Optional<IGGAPIOwnerVerifier> ownerVerifier) {
		this.authorizationManager = authorizationManager;
		this.authenticationManager = authenticationManager;
		this.tenantVerifier = tenantVerifier;
		this.ownerVerifier = ownerVerifier;
	}

	public void setDomains(Set<IGGAPIDomain> domains) {
		this.domains = domains;
		log.info("Garganttua API Security Engine initalisation");
		this.accessRulesRegistry = new GGAPIAccessRulesRegistry(domains);
		
		this.authenticatorDomain = domains.stream().filter(domain -> 
			domain.getSecurity().authenticatorInfos()!=null?true:false
		).findFirst();
	}

	@Override
	public List<String> getAuthorities() {
		return this.accessRulesRegistry.getAuthorities();
	}
	
	@Override
	public boolean isAuthenticatorEntity(Object entity) {
		boolean isAuthenticator = false;
		
		if( this.authenticatorDomain.isPresent() )
			isAuthenticator = this.authenticatorDomain.get().getEntity().getValue0().equals(entity.getClass());
		
		return isAuthenticator;
	}

	@Override
	public Object applySecurityOnAuthenticatorEntity(Object entity) throws GGAPIException {
		if( this.isAuthenticatorEntity(entity) ) {
			if( this.authenticationManager.isPresent() ) {
				return this.authenticationManager.get().applySecurityOnAuthenticatorEntity(entity);
			}
		}
		return null;
	}

	@Override
	public IGGAPIAuthorization authenticate(Object entity) throws GGAPIException {
		if( this.isAuthenticatorEntity(entity) ) {
			if( this.authenticationManager.isPresent() ) {
				if( this.authenticationManager.get().authenticate(entity) ) {
					return this.authorizationManager.get().createAuthorization(entity);
				}
			}
		}
		return null;
	}

	@Override
	public IGGAPIAuthorization validateAuthorization(byte[] authorization) throws GGAPIException {
		if( this.authorizationManager.isPresent() ) {
			return this.authorizationManager.get().validateAuthorization(authorization);
		}
		return null;
	}

	@Override
	public void verifyTenant(IGGAPICaller caller, IGGAPIAuthorization authorization) throws GGAPIException {
		if( this.tenantVerifier.isPresent() ) {
			this.tenantVerifier.get().verifyTenant(caller, authorization);
		}
	}

	@Override
	public void verifyOwner(IGGAPICaller caller, IGGAPIAuthorization authorization) throws GGAPIException {
		if( this.ownerVerifier.isPresent() ) {
			this.ownerVerifier.get().verifyOwner(caller, authorization);
		}
	}
}
