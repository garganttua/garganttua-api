package com.garganttua.api.spec.security;

import java.util.Collection;
import java.util.Date;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorization {
	
	byte[] toByteArray() throws GGAPIException;

	String getUuid();

	String getTenantId();
	
	String getOwnerId();
	
	Collection<String> getAuthorities();
	
	Date getCreationDate();
	
	Date getExpirationDate();
	
	String getSigningKeyUuid();
	
	byte[] getSignature();

}
