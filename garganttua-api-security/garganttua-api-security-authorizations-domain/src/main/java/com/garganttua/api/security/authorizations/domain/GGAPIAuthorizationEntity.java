package com.garganttua.api.security.authorizations.domain;

import java.util.Collection;
import java.util.Date;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.spec.security.IGGAPIAuthorization;

public abstract class GGAPIAuthorizationEntity extends GenericGGAPIEntity implements IGGAPIAuthorization {

	public static final String domain = "keys";
	
	public String ownerId;
	
	public Collection<String> authorities;
	
	public Date creationDate;
	
	public Date expirationDate;
	
	public String signingKeyId;
	
}
