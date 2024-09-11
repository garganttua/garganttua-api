package com.garganttua.api.security.core.engine;

import java.util.Optional;
import java.util.Set;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManagerIfPresentMethod;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManagerIfPresentMethod;
import com.garganttua.api.spec.security.IGGAPIOrElseMethod;
import com.garganttua.api.spec.security.IGGAPIOwnerVerifier;
import com.garganttua.api.spec.security.IGGAPIOwnnerVerifierIfPresentMethod;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.security.IGGAPITenantVerifier;
import com.garganttua.api.spec.security.IGGAPITenantVerifierIfPresentMethod;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPISecurityEngine implements IGGAPISecurityEngine {

	protected Optional<IGGAPIDomain> authenticatorDomain;
	
	protected Optional<IGGAPIAuthenticationManager> authenticationManager; 
	protected Optional<IGGAPIAuthorizationManager> authorizationManager;
	protected Optional<IGGAPITenantVerifier> tenantVerifier;
	protected Optional<IGGAPIOwnerVerifier> ownerVerifier;
	
	protected Set<IGGAPIDomain> domains; 

	protected GGAPISecurityEngine(Set<IGGAPIDomain> domains, Optional<IGGAPIAuthorizationManager> authorizationManager, Optional<IGGAPIAuthenticationManager> authenticationManager, Optional<IGGAPITenantVerifier> tenantVerifier, Optional<IGGAPIOwnerVerifier> ownerVerifier) {
		log.info("Garganttua API Security Engine initalisation");
		this.authorizationManager = authorizationManager;
		this.authenticationManager = authenticationManager;
		this.tenantVerifier = tenantVerifier;
		this.ownerVerifier = ownerVerifier;
		this.setDomains(domains);
	}

	private void setDomains(Set<IGGAPIDomain> domains) {
		this.domains = domains;

		this.authenticatorDomain = domains.stream().filter(domain -> {
			return domain.getSecurity().authenticatorInfos()!=null?true:false;
		}).findFirst();
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
	public IGGAPIAuthentication authenticate(IGGAPIAuthentication authentication) throws GGAPIException {
		if( this.authenticationManager.isPresent() ) {
			authentication = this.authenticationManager.get().authenticate(authentication);
			if( authentication.isAuthenticated() && this.authorizationManager.isPresent() ) {
				return this.authorizationManager.get().createAuthorization(authentication);
			} else {
				return authentication;
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

	@Override
	public void ifAuthorizationManagerPresentOrElse(IGGAPIAuthorizationManagerIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod) throws GGAPIException {
		if( this.authorizationManager.isPresent() ){
			method.ifPresent(this.authorizationManager.get(), caller);
		} else {
			if( orElseMethod != null ) {
				orElseMethod.orElse();
			}
		}
	}

	@Override
	public void ifAuthenticationManagerPresentOrElse(IGGAPIAuthenticationManagerIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod)
			throws GGAPIException {
		if( this.authenticationManager.isPresent() ){
			method.ifPresent(this.authenticationManager.get(), caller);
		} else {
			if( orElseMethod != null ) {
				orElseMethod.orElse();
			}
		}
	}

	@Override
	public void ifTenantVerifierPresentOrElse(IGGAPITenantVerifierIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod) throws GGAPIException {
		if( this.tenantVerifier.isPresent() ){
			method.ifPresent(this.tenantVerifier.get(), caller);
		} else {
			if( orElseMethod != null ) {
				orElseMethod.orElse();
			}
		}
	}

	@Override
	public void ifOwnerVerifierPresentOrElse(IGGAPIOwnnerVerifierIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod) throws GGAPIException {
		if( this.ownerVerifier.isPresent() ){
			method.ifPresent(this.ownerVerifier.get(), caller);
		} else {
			if( orElseMethod != null ) {
				orElseMethod.orElse();
			}
		}
	}

	@Override
	public void ifAuthorizationManagerPresent(IGGAPIAuthorizationManagerIfPresentMethod method, IGGAPICaller caller)
			throws GGAPIException {
		this.ifAuthorizationManagerPresentOrElse(method, caller, null);
	}

	@Override
	public void ifAuthenticationManagerPresent(IGGAPIAuthenticationManagerIfPresentMethod method, IGGAPICaller caller)
			throws GGAPIException {
		this.ifAuthenticationManagerPresentOrElse(method, caller, null);
	}

	@Override
	public void ifTenantVerifierPresent(IGGAPITenantVerifierIfPresentMethod method, IGGAPICaller caller)
			throws GGAPIException {
		this.ifTenantVerifierPresentOrElse(method, caller, null);
	}

	@Override
	public void ifOwnerVerifierPresent(IGGAPIOwnnerVerifierIfPresentMethod method, IGGAPICaller caller)
			throws GGAPIException {
		this.ifOwnerVerifierPresentOrElse(method, caller, null);
	}
}
