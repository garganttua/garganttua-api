package com.garganttua.api.core.security.authentication;

import java.util.List;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticate;
import com.garganttua.api.spec.security.authentication.IAuthentication;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuthentication implements IAuthentication {

	protected IDomainContext<?> domainContext;

	public AbstractAuthentication(IDomainContext<?> domainContext) {
		this.domainContext = domainContext;
	}

	protected IDomainContext<?> authenticatorDomainContext;

	protected AuthenticatorInfos authenticatorInfos;

	protected Object authorization;

	protected boolean authenticated = false;

	protected Object principal;

	protected Object credential;

	protected String tenantId;

	protected String ownerId;

	protected List<String> authorities;

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
