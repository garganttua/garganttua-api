package com.garganttua.api.security.keys;

import java.security.Key;

import io.jsonwebtoken.SignatureAlgorithm;

public interface IGGAPIKeyManager {
	
	Key getKeyForCiphering(String realm) throws GGAPIKeyExpiredException;
	
	Key getKeyForUnciphering(String realm) throws GGAPIKeyExpiredException;
	
	void renew(String realm);
	
	void createRealm(String realm, SignatureAlgorithm algo);
	
	void createRealm(String realm, SignatureAlgorithm algo, GGAPIKeyExpiration expiration);

}
