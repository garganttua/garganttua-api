package com.garganttua.api.core.security.authentication;

import java.util.List;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.service.GGAPIService;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwnerId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticate;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticated;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticatorInfos;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticatorService;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthorization;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationCredentials;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationFindPrincipal;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationPrincipal;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorScope;
import com.garganttua.api.spec.service.IGGAPIService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractGGAPIAuthentication extends GGAPIService {
	
	public AbstractGGAPIAuthentication(IGGAPIDomain domain) {
		super(domain);
	}

	@GGAPIAuthenticationAuthenticatorService
	protected IGGAPIService authenticatorService;

	@GGAPIAuthenticationAuthenticatorInfos
	protected GGAPIAuthenticatorInfos authenticatorInfos;
	
	@GGAPIAuthenticationAuthorization
	protected Object authorization;

	@GGAPIAuthenticationAuthenticated
	protected boolean authenticated = false;
	
	@GGAPIAuthenticationPrincipal
	protected Object principal;

	@GGAPIAuthenticationCredentials
	protected Object credential;

	@GGAPIEntityTenantId
	protected String tenantId;
	
	@GGAPIEntityOwnerId
	protected String ownerId;

	@GGAPIAuthenticationAuthorities
	protected List<String> authorities;

	@GGAPIAuthenticationAuthenticate
	public void authenticate() throws GGAPIException {	 
		boolean authenticator = GGAPIEntityAuthenticatorHelper.isAuthenticator(this.principal);
		if( authenticator ) {
			this.checkPrincipal();
		}
		
		this.doAuthentication();
		if( this.authenticated && authenticator ) {
			this.authorities = GGAPIEntityAuthenticatorHelper.getAuthorities(this.principal);
		} 
	}

	@GGAPIAuthenticationFindPrincipal
	protected void findPrincipal() throws GGAPIException {
		if( this.authenticatorService != null ) {
		  IGGAPICaller caller;
		  
		  if( this.authenticatorInfos.scope() == GGAPIAuthenticatorScope.tenant) {
		    caller = GGAPICaller.createTenantCaller(this.tenantId);
		  } else {
		    caller = GGAPICaller.createSuperCaller();
		  }

      Object principal = this.doFindPrincipal(caller);
			if( principal == null ) {
				log.atWarn().log("Principal identified by "+this.principal+" is not found");
				return;
			}	
			this.principal = principal;
			this.tenantId = GGAPIEntityHelper.getTenantId(this.principal);
			try {
				this.ownerId = GGAPIEntityHelper.getOwnerId(principal);
			} catch (GGAPIException e) {
				log.atTrace().log("Error triing to get ownerId of principal identified by "+this.principal, e);
			}
		} else {
			log.atWarn().log("Principal identified by "+this.principal+" indicated to be found but no authenticator service provided"); 
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, "Principal identified by "+this.principal+" indicated to be found but no authenticator service provided");
		}
	}

	protected abstract Object doFindPrincipal(IGGAPICaller caller);

	protected void checkPrincipal() throws GGAPISecurityException, GGAPIException {
		if( !GGAPIEntityAuthenticatorHelper.isAccountNonExpired(this.principal) ) {
			this.authenticated = false;
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Authenticator expired");
		}
		if( !GGAPIEntityAuthenticatorHelper.isAccountNonLocked(this.principal) ) {
			this.authenticated = false;
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Authenticator locked");
		}
		if( !GGAPIEntityAuthenticatorHelper.isCredentialsNonExpired(this.principal) ) {
			this.authenticated = false;
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Authenticator's credentials expired");
		}
		if( !GGAPIEntityAuthenticatorHelper.isEnabled(this.principal) ) {
			this.authenticated = false;
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Authenticator disabled");
		}
	}

	protected abstract void doAuthentication() throws GGAPIException;

}
