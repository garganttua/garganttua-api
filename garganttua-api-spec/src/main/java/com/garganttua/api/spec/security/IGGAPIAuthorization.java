package com.garganttua.api.spec.security;

public interface IGGAPIAuthorization {
	
	byte[] toByteArray();

	String getUuid();

	String getTenantId();

}
