package com.garganttua.api.core.security.authentication;

import java.util.List;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticate;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticated;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticatorInfos;
import com.garganttua.api.spec.security.authentication.IAuthentication;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticatorService;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthorities;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthorization;
import com.garganttua.api.spec.security.annotations.AuthenticationCredentials;
import com.garganttua.api.spec.security.annotations.AuthenticationFindPrincipal;
import com.garganttua.api.spec.security.annotations.AuthenticationPrincipal;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuthentication implements IAuthentication {

	protected IDomainContext<?> domainContext;

	public AbstractAuthentication(IDomainContext<?> domainContext) {
		this.domainContext = domainContext;
	}

	@AuthenticationAuthenticatorService
	protected IDomainContext<?> authenticatorDomainContext;

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
	public void findPrincipal() throws CoreException {
		if (this.authenticatorDomainContext != null) {
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
			this.tenantId = getFieldValue(this.principal,
					this.authenticatorDomainContext.getTenantIdFieldAddress());
			try {
				this.ownerId = getFieldValue(this.principal,
						this.authenticatorDomainContext.getOwnerIdFieldAddress());
			} catch (Exception e) {
				log.atTrace().log("Error trying to get ownerId of principal identified by " + this.principal, e);
			}
		} else {
			log.atWarn().log("Principal identified by " + this.principal
					+ " indicated to be found but no authenticator domain context provided");
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, "Principal identified by " + this.principal
					+ " indicated to be found but no authenticator domain context provided");
		}
	}

	protected abstract Object doFindPrincipal(ICaller caller);

	protected void checkPrincipal() throws CoreException {
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

	@SuppressWarnings("unchecked")
	private static <T> T getFieldValue(Object entity, ObjectAddress address) {
		if (address == null) return null;
		try {
			IReflection reflection = DefaultMapper.reflection();
			return (T) reflection.getFieldValue(entity, address);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean isAuthenticated() {
		return this.authenticated;
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}

	@Override
	public Object getCredential() {
		return this.credential;
	}

	@Override
	public Object getAuthorization() {
		return this.authorization;
	}

	@Override
	public List<String> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getTenantId() {
		return this.tenantId;
	}

	@Override
	public String getOwnerId() {
		return this.ownerId;
	}
}
