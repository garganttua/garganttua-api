package com.garganttua.api.security.authorizations.domain;

import java.util.Date;
import java.util.List;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.security.IGGAPIAuthorization;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class GGAPIAuthorizationEntity extends GenericGGAPIEntity implements IGGAPIAuthorization {

	public GGAPIAuthorizationEntity(String uuid, String tenantId, String ownerId, List<String> authorities,
			Date creationDate, Date expirationDate, String signingKeyUuid) {
		this.uuid = uuid;
		this.id = ownerId;
		this.tenantId = tenantId;
		this.ownerId = ownerId; 
		this.authorities = authorities;
		this.creationDate = creationDate;
		this.expirationDate = expirationDate;
		this.signingKeyUuid = signingKeyUuid;
	}

	public static final String domain = "authorizations";
	
	@GGAPIEntityMandatory
	public String ownerId;
	
	@GGAPIEntityMandatory
	public List<String> authorities;
	
	@GGAPIEntityMandatory
	public Date creationDate;
	
	@GGAPIEntityMandatory
	public Date expirationDate;
	
	@GGAPIEntityMandatory
	public String signingKeyUuid;
	
	@GGAPIEntityAuthorizeUpdate
	public boolean revoked = false;
	
	public void revoke() {
		this.revoked = true;
	}
	
	public boolean isRevoked() {
		return this.revoked;
	}
}
