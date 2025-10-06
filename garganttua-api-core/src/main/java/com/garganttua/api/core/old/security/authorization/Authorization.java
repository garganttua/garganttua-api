package com.garganttua.api.core.security.authorization;

import java.util.Date;
import java.util.List;

import com.garganttua.api.core.entity.GenericEntity;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.entity.annotations.EntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.EntityMandatory;
import com.garganttua.api.spec.entity.annotations.EntityOwned;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;
import com.garganttua.api.spec.security.annotations.AuthenticatorAccountNonExpired;
import com.garganttua.api.spec.security.annotations.AuthenticatorAccountNonLocked;
import com.garganttua.api.spec.security.annotations.AuthenticatorAuthorities;
import com.garganttua.api.spec.security.annotations.AuthenticatorCredentialsNonExpired;
import com.garganttua.api.spec.security.annotations.AuthenticatorEnabled;
import com.garganttua.api.spec.security.annotations.AuthorizationAuthorities;
import com.garganttua.api.spec.security.annotations.AuthorizationCreation;
import com.garganttua.api.spec.security.annotations.AuthorizationExpiration;
import com.garganttua.api.spec.security.annotations.AuthorizationRevoked;
import com.garganttua.api.spec.security.annotations.AuthorizationValidate;
import com.garganttua.api.spec.security.annotations.AuthorizationValidateAgainst;
import com.garganttua.api.spec.security.authorization.IAuthorization;

import lombok.Getter;

@EntityOwned
public abstract class Authorization extends GenericEntity implements IAuthorization {

	public Authorization(byte[] raw) throws SecurityException {
		this.decodeFromRaw(raw);
	}

	protected abstract void decodeFromRaw(byte[] raw) throws SecurityException;

	public Authorization(String uuid, String id, String tenantId, String ownerId, List<String> authorities,
			Date creationDate, Date expirationDate) throws SecurityException {
		super(uuid, id);
		this.tenantId = tenantId;
		if (tenantId == null)
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Tenant uuid mandatory");
		this.ownerId = ownerId;
		if (ownerId == null)
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Owner uuid mandatory");
		this.authorities = authorities;
		this.creationDate = creationDate;
		if (creationDate == null)
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Creation date mandatory");
		this.expirationDate = expirationDate;
		if (expirationDate == null)
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Expiration date mandatory");
	}

	public Authorization() {
		super();
	}

	@EntityMandatory
	@Getter
	@EntityOwnerId
	protected String ownerId;

	@EntityMandatory
	@Getter
	@AuthorizationAuthorities
	@AuthenticatorAuthorities
	protected List<String> authorities;

	@EntityMandatory
	@Getter
	@AuthorizationCreation
	protected Date creationDate;

	@EntityMandatory
	@Getter
	@AuthorizationExpiration
	protected Date expirationDate;

	@EntityAuthorizeUpdate
	@Getter
	@AuthorizationRevoked
	protected Boolean revoked = false;

	@AuthenticatorAccountNonExpired
	@AuthenticatorAccountNonLocked
	@AuthenticatorCredentialsNonExpired
	@AuthenticatorEnabled
	protected Boolean enabled = true;

	@Override
	public void revoke() {
		this.revoked = true;
	}

	@AuthorizationValidateAgainst
	@Override
	public void validateAgainst(IAuthorization authorizationReference, Object... args) throws CoreException {
		this.isExpired();
		this.isRevoked();
		this.doValidationAgainst(authorizationReference, args);
	}

	protected abstract void doValidationAgainst(IAuthorization authorization, Object... args)
			throws SecurityException;

	@AuthorizationValidate
	@Override
	public void validate(Object... args) throws CoreException {
		this.isExpired();
		this.isRevoked();
		this.doValidation(args);
	}

	@Override
	public void isRevoked() throws SecurityException {
		if (this.revoked) {
			throw new SecurityException(CoreExceptionCode.TOKEN_REVOKED, "Token revoked");
		}
	}

	public void isExpired() throws SecurityException {
		if (new Date().after(this.expirationDate)) {
			this.enabled = false;
			throw new SecurityException(CoreExceptionCode.TOKEN_EXPIRED, "Token expired");
		}
	}

	protected abstract void doValidation(Object... args) throws SecurityException;
}
