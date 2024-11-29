package com.garganttua.api.core.security.authentication;

import java.util.List;

import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticate;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticated;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticatorInfos;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticatorService;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthorization;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationCredentials;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationPrincipal;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.service.IGGAPIService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractGGAPIAuthentication {
	
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

	@GGAPIAuthenticationAuthorities
	protected List<String> authorities;

	@GGAPIAuthenticationAuthenticate
	public void authenticate(Boolean findPrincipal) throws GGAPIException {
		if( findPrincipal && this.authenticatorService != null ) {
			Object principal = this.findPrincipal();
			
			if( principal == null ) {
				log.atWarn().log("Principal identified by "+this.principal+" is not found");
				return;
			}
			
			this.principal = principal;
			this.authorities = GGAPIEntityAuthenticatorHelper.getAuthorities(principal);
		} else if( !findPrincipal && this.authenticatorService == null ) {
//			this.principal = this.credential;
		} else {
			log.atWarn().log("Principal indicated to be found but no authenticator service provided"); 
			return;
		}

		this.doAuthentication();
			 
		if( this.authenticated && findPrincipal ) {
			this.checkPrincipal();
		} 
	}

	protected abstract Object findPrincipal();

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
