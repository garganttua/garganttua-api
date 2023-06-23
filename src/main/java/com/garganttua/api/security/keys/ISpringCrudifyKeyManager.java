package com.garganttua.api.security.keys;

import java.security.Key;

import io.jsonwebtoken.SignatureAlgorithm;

public interface ISpringCrudifyKeyManager {
	
	Key getKeyForCiphering(String realm) throws SpringCrudifyKeyExpiredException;
	
	Key getKeyForUnciphering(String realm) throws SpringCrudifyKeyExpiredException;
	
	void renew(String realm);
	
	void createRealm(String realm, SignatureAlgorithm algo);
	
	void createRealm(String realm, SignatureAlgorithm algo, SpringCrudifyKeyExpiration expiration);

}
