package com.garganttua.api.security.spring.core.keys;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;

public interface IGGAPISpringKeyProvider {

	IGGAPIKeyRealm getRealm(String tenantId, String ownerId, String keyRealmName, String algorithm) throws GGAPISecurityException;

	IGGAPIKeyRealm createRealm(String tenantId, String ownerId, String keyRealmName, String algorithm) throws GGAPISecurityException;

	IGGAPIKeyRealm revokeRealm(String tenantId, String ownerId, String keyRealmName, String algorithm) throws GGAPISecurityException;

	IGGAPIKeyRealm revokeAllRealms(String tenantId, String ownerId, String keyRealmName) throws GGAPISecurityException;
}
