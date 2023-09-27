package com.garganttua.api.security.keys;

import com.garganttua.api.engine.GGAPIEngineException;

import io.jsonwebtoken.SignatureAlgorithm;

public class GGAPIKeyRealms {

	public static IGGAPIKeyRealm createRealm(String name, String algo, GGAPIKey ciphering, GGAPIKey unciphering) {
		IGGAPIKeyRealm keyRealm = null;
		
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.forName(algo);
		switch (signatureAlgorithm ) {
		default:
		case HS256:
		case HS384:
		case HS512:
			keyRealm = new GGAPISymetricKeyRealm(name, ciphering);
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
			keyRealm = new GGAPIAsymetricKeyRealm(name, unciphering, ciphering);
			break;
		}
		return keyRealm;
	}
	
	
	public static IGGAPIKeyRealm createRealm(String name, String algo, GGAPIKeyExpiration expiration) {
		IGGAPIKeyRealm keyRealm = null;
		
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.forName(algo);
		switch (signatureAlgorithm ) {
		default:
		case HS256:
		case HS384:
		case HS512:
			keyRealm = new GGAPISymetricKeyRealm(name, algo, expiration);
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
			keyRealm = new GGAPIAsymetricKeyRealm(name, algo, expiration);
			break;
		}
		return keyRealm;
	}

}