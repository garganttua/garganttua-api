package com.garganttua.api.security.keys;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.SignatureAlgorithm;

public class GGAPIInMemoryKeyManager implements IGGAPIKeyManager {

	private Map<String, IGGAPIKeyRealm> realms = new HashMap<String, IGGAPIKeyRealm>();
	
	@Override
	public Key getKeyForCiphering(String realm) throws GGAPIKeyExpiredException {
		IGGAPIKeyRealm keyRealm = this.realms.get(realm);
		return keyRealm.getCipheringKey();
	}

	@Override
	public Key getKeyForUnciphering(String realm) throws GGAPIKeyExpiredException {
		IGGAPIKeyRealm keyRealm = this.realms.get(realm);
		return keyRealm.getUncipheringKey();
	}

	@Override
	public void renew(String realm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRealm(String realm, SignatureAlgorithm algo) {
		this.createRealm(realm, algo, null);
	}

	@Override
	public void createRealm(String realm, SignatureAlgorithm algo, GGAPIKeyExpiration expiration) {

		IGGAPIKeyRealm keyRealm = null;
		
		switch (algo) {
		default:
		case HS256:
		case HS384:
		case HS512:
			keyRealm = new GGAPISymetricKeyRealm(algo, expiration);
			break;
		case ES256:
		case ES384:
		case ES512:
		case PS256:
		case PS384:
		case PS512:
		case RS256:
		case RS384:
		case RS512:
			break;
		
		}

		this.realms.putIfAbsent(realm, keyRealm);
	}

}
