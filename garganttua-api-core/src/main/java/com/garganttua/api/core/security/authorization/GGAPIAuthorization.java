package com.garganttua.api.core.security.authorization;

import java.util.Date;
import java.util.List;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwnerId;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonLocked;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorCredentialsNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorEnabled;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationCreation;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationExpiration;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationRevoked;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationValidate;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationValidateAgainst;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorization;

import lombok.Getter;

@GGAPIEntityOwned
public abstract class GGAPIAuthorization extends GenericGGAPIEntity implements IGGAPIAuthorization {

	public GGAPIAuthorization(byte[] raw) throws GGAPISecurityException {
		this.decodeFromRaw(raw);
	}

	protected abstract void decodeFromRaw(byte[] raw) throws GGAPISecurityException;

	public GGAPIAuthorization(String uuid, String id, String tenantId, String ownerId, List<String> authorities,
			Date creationDate, Date expirationDate) {
		super(uuid, id);
		this.tenantId = tenantId;
		this.ownerId = ownerId; 
		this.authorities = authorities;
		this.creationDate = creationDate;
		this.expirationDate = expirationDate;
	}

	public GGAPIAuthorization(){
		super();
	}

	@GGAPIEntityMandatory
	@Getter
	@GGAPIEntityOwnerId
	protected String ownerId;
	
	@GGAPIEntityMandatory
	@Getter
	@GGAPIAuthorizationAuthorities
	@GGAPIAuthenticatorAuthorities
	protected List<String> authorities;
	
	@GGAPIEntityMandatory
	@Getter
	@GGAPIAuthorizationCreation
	protected Date creationDate;
	
	@GGAPIEntityMandatory
	@Getter
	@GGAPIAuthorizationExpiration
	protected Date expirationDate;
	
	@GGAPIEntityAuthorizeUpdate
	@Getter
	@GGAPIAuthorizationRevoked
	protected Boolean revoked = false;
	
	@GGAPIAuthenticatorAccountNonExpired
	@GGAPIAuthenticatorAccountNonLocked
	@GGAPIAuthenticatorCredentialsNonExpired
	@GGAPIAuthenticatorEnabled
	protected Boolean enabled = true;
	
	@Override
	public void revoke() {
		this.revoked = true;
	}
	
	@GGAPIAuthorizationValidateAgainst
	@Override
	public void validateAgainst(IGGAPIAuthorization authorizationReference, Object ...args) throws GGAPIException {
		isRevokedOrExpired(authorizationReference);
		this.doValidationAgainst(authorizationReference, args);
	}
	
	protected abstract void doValidationAgainst(IGGAPIAuthorization authorization, Object ...args) throws GGAPISecurityException;

	@GGAPIAuthorizationValidate
	@Override
	public void validate(Object ...args) throws GGAPIException {
		isRevokedOrExpired(this);
		this.doValidation(args);
	}

	private static void isRevokedOrExpired(IGGAPIAuthorization authorization) throws GGAPISecurityException {
		if( ((GGAPIAuthorization) authorization).revoked ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_REVOKED, "Token revoked");
		}
		if( new Date().after( ((GGAPIAuthorization) authorization).expirationDate) ) {
			 ((GGAPIAuthorization) authorization).enabled = false;
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_EXPIRED, "Token expired");
		}
	}

	protected abstract void doValidation(Object ...args) throws GGAPISecurityException;
}
