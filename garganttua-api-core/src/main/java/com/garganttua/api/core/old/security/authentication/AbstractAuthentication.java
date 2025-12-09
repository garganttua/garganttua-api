package com.garganttua.api.core.security.authentication;

import java.util.List;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.core.service.Service;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticate;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticated;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticatorInfos;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticatorService;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthorities;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthorization;
import com.garganttua.api.spec.security.annotations.AuthenticationCredentials;
import com.garganttua.api.spec.security.annotations.AuthenticationFindPrincipal;
import com.garganttua.api.spec.security.annotations.AuthenticationPrincipal;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.service.IService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuthentication extends Service {

	public AbstractAuthentication(IDomain domain) {
		super(domain);
	}

	@AuthenticationAuthenticatorService
	protected IService authenticatorService;

	@AuthenticationAuthenticatorInfos
	protected AuthenticatorInfos authenticatorInfos;

	@AuthenticationAuthorization
	protected Object authorization;

	@AuthenticationAuthenticated
	protected boolean authenticated = false;

	@AuthenticationPrincipal
	protected Object principal;

	@AuthenticationCredentials
	protected Object credential;

	@EntityTenantId
	protected String tenantId;

	@EntityOwnerId
	protected String ownerId;

	@AuthenticationAuthorities
	protected List<String> authorities;

	@AuthenticationAuthenticate
	public void authenticate() throws CoreException {
		boolean authenticator = EntityAuthenticatorHelper.isAuthenticator(this.principal);
		if (authenticator) {
			this.checkPrincipal();
		}

		this.doAuthentication();
		if (this.authenticated && authenticator) {
			this.authorities = EntityAuthenticatorHelper.getAuthorities(this.principal);
		}
	}

	@AuthenticationFindPrincipal
	protected void findPrincipal() throws CoreException {
		if (this.authenticatorService != null) {
			ICaller caller;

			if (this.authenticatorInfos.scope() == AuthenticatorScope.tenant) {
				caller = Caller.createTenantCaller(this.tenantId);
			} else {
				caller = Caller.createSuperCaller();
			}

			Object principal = this.doFindPrincipal(caller);
			if (principal == null) {
				log.atWarn().log("Principal identified by " + this.principal + " is not found");
				return;
			}
			this.principal = principal;
			this.tenantId = EntityHelper.getTenantId(this.principal);
			try {
				this.ownerId = EntityHelper.getOwnerId(principal);
			} catch (CoreException e) {
				log.atTrace().log("Error triing to get ownerId of principal identified by " + this.principal, e);
			}
		} else {
			log.atWarn().log("Principal identified by " + this.principal
					+ " indicated to be found but no authenticator service provided");
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, "Principal identified by " + this.principal
					+ " indicated to be found but no authenticator service provided");
		}
	}

	protected abstract Object doFindPrincipal(ICaller caller);

	protected void checkPrincipal() throws SecurityException, CoreException {
		if (!EntityAuthenticatorHelper.isAccountNonExpired(this.principal)) {
			this.authenticated = false;
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authenticator expired");
		}
		if (!EntityAuthenticatorHelper.isAccountNonLocked(this.principal)) {
			this.authenticated = false;
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authenticator locked");
		}
		if (!EntityAuthenticatorHelper.isCredentialsNonExpired(this.principal)) {
			this.authenticated = false;
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
					"Authenticator's credentials expired");
		}
		if (!EntityAuthenticatorHelper.isEnabled(this.principal)) {
			this.authenticated = false;
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authenticator disabled");
		}
	}

	protected abstract void doAuthentication() throws CoreException;

}
