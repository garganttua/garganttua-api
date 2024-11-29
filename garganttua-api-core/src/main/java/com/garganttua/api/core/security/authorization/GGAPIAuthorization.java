package com.garganttua.api.core.security.authorization;

import java.util.Date;
import java.util.List;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.security.authorization.jwt.GGAPIJWTAuthorization;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwnerId;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationCreation;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationExpiration;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationRevoked;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationValidate;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationValidateAgainst;

import lombok.Getter;

@GGAPIEntityOwned
@GGAPIAuthenticator
public abstract class GGAPIAuthorization extends GenericGGAPIEntity {

	public GGAPIAuthorization(String uuid, String id, String tenantId, String ownerUuid, List<String> authorities,
			Date creationDate, Date expirationDate) {
		this.uuid = uuid;
		this.id = id;
		this.tenantId = tenantId;
		this.ownerUuid = ownerUuid; 
		this.authorities = authorities;
		this.creationDate = creationDate;
		this.expirationDate = expirationDate;
	}

	@GGAPIEntityMandatory
	@Getter
	@GGAPIEntityOwnerId
	protected String ownerUuid;
	
	@GGAPIEntityMandatory
	@Getter
	@GGAPIAuthorizationAuthorities
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
	protected boolean revoked = false;
	
	public void revoke() {
		this.revoked = true;
	}
	
	@GGAPIAuthorizationValidateAgainst
	public void validateAgainst(GGAPIJWTAuthorization authorization) throws GGAPIException {
		this.validate();
		authorization.validate();
		this.doValidationAgainst(authorization);
	}
	
	protected abstract void doValidationAgainst(GGAPIJWTAuthorization authorization) throws GGAPISecurityException;

	@GGAPIAuthorizationValidate
	public void validate() throws GGAPIException {
		if( this.revoked ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_REVOKED, "Token revoked");
		}
		if( new Date().after(this.expirationDate) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_EXPIRED, "Token expired");
		}
		this.doValidation();
	}

	protected abstract void doValidation() throws GGAPIException;
}
