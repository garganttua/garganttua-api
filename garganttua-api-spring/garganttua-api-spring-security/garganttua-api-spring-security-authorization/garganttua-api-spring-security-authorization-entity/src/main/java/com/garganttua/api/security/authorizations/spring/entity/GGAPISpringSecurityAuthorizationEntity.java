package com.garganttua.api.security.authorizations.spring.entity;

import java.util.Date;
import java.util.List;

import com.garganttua.api.security.authorizations.domain.GGAPIAuthorizationEntity;

public abstract class GGAPISpringSecurityAuthorizationEntity extends GGAPIAuthorizationEntity {

	public GGAPISpringSecurityAuthorizationEntity(String uuid, String tenantId, String ownerId, List<String> authorities, Date creationDate,
			Date expirationDate, String signingKeyUuid) {
		super(uuid, tenantId, ownerId, authorities, creationDate, expirationDate, signingKeyUuid);
	}
	
	public GGAPISpringSecurityAuthorizationEntity() {
		super();
	}
	
	@Override
	public String getOwnerId() {
		return this.ownerId;
	}

	@Override
	public List<String> getAuthorities() {
		return this.authorities;
	}

	@Override
	public Date getCreationDate() {
		return this.creationDate;
	}

	@Override
	public Date getExpirationDate() {
		return this.expirationDate;
	}

	@Override
	public String getSigningKeyUuid() {
		return this.signingKeyUuid;
	}

}
