package com.garganttua.api.security.keys;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class GGAPISymetricKeyRealm extends AbstractGGAPIKeyRealm {

	private GGAPIKey key;
	private SignatureAlgorithm algo;
	private String realm;

	public GGAPISymetricKeyRealm(String realm, SignatureAlgorithm algo, GGAPIKeyExpiration expiration) {
		super(algo, expiration);
		this.realm = realm;
		this.algo = algo;
		if( expiration != null ) {
			this.key.setExpiration(new Date(System.currentTimeMillis() + expiration.unit().toMillis(expiration.time())));
		}
	}

	@Override
	protected void getKey(SignatureAlgorithm algo) {
		SecretKey key__ = Keys.secretKeyFor(algo);
		this.key = new GGAPIKey(UUID.randomUUID().toString(), this.realm, key__.getAlgorithm(), null, GGAPIKeyType.SYMETRIC, key__.getEncoded());
	}

	@Override
	protected GGAPIKey getCipheringKey_() {
		return this.key;
	}

	@Override
	protected GGAPIKey getUncipheringKey_() {
		return this.key;
	}

	@Override
	public String getName() {
		return this.realm;
	}

}
