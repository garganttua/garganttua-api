package com.garganttua.api.security.spring.core.keys;

import java.util.Date;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;

public interface IGGAPISpringKeyProvider {

	IGGAPIKeyRealm getRealm(IGGAPIAuthenticator authenticator, String keyRealmName) throws GGAPISecurityException;

	IGGAPIKeyRealm createRealm(IGGAPIAuthenticator authenticator, String keyRealmName, String algorithm,
			Date expiration) throws GGAPISecurityException;
}
