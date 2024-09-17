package com.garganttua.api.security.spring.core.keys;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;

public interface IGGAPISpringKeyProvider {

	IGGAPIKeyRealm getRealm(GGAPISpringSecurityKeyEntityRequest request) throws GGAPISecurityException;

	IGGAPIKeyRealm createRealm(GGAPISpringSecurityKeyEntityRequest request) throws GGAPISecurityException;

	IGGAPIKeyRealm revokeRealm(GGAPISpringSecurityKeyEntityRequest request) throws GGAPISecurityException;

	IGGAPIKeyRealm revokeAllRealms(GGAPISpringSecurityKeyEntityRequest request) throws GGAPISecurityException;

	IGGAPIKeyRealm getRealm(String realmUuid) throws GGAPIException;
}
