package com.garganttua.api.security.spring.core.keys;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;

public record GGAPISpringSecurityKeyEntityRequest(String tenantId, String ownerId, String keyRealmName, String algorithm) {

	public void validate() throws GGAPISecurityException {
		// TODO Auto-generated method stub
		
	}

}
