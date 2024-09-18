package com.garganttua.api.spec.security;

import java.util.Date;
import java.util.List;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorization {
	
	byte[] toByteArray() throws GGAPIException;

	String getUuid();

	String getTenantId();
	
	String getOwnerId();
	
	List<String> getAuthorities();
	
	Date getCreationDate();
	
	Date getExpirationDate();
	
	String getSigningKeyUuid();
	
	boolean isRevoked();
	
	void revoke();
	
	byte[] getSignature();
	
	void validateAgainst(byte[] authorization) throws GGAPIException;

}
