package com.garganttua.api.security.keys;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class GGAPISymetricKeyRealm extends AbstractGGAPIKeyRealm {

	private GGAPIKey key;
	private String algo;
	private String realm;

	public GGAPISymetricKeyRealm(String realm, String algo, GGAPIKey key) {
		super();
		this.realm = realm;
		this.algo = algo;
		this.key = key;
	}

	public GGAPISymetricKeyRealm(String realm, String algo, GGAPIKeyExpiration expiration) {
		super(algo, expiration);
		this.realm = realm;
		this.algo = algo;
		if (expiration != null) {
			this.key.setExpiration(
					new Date(System.currentTimeMillis() + expiration.unit().toMillis(expiration.time())));
		}
	}

	@Override
	protected void getKey(String algo) {
		SecretKey key__ = Keys.secretKeyFor(SignatureAlgorithm.forName(algo));
		this.key = new GGAPIKey(UUID.randomUUID().toString(), key__.getAlgorithm(), null,
				GGAPIKeyType.SYMETRIC, key__.getEncoded());
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

	@Override
	public String getAlgo() {
		return this.algo;
	}
	
	@Override
	public boolean equals(Object object) {
		try {
			return this.getAlgo().equals(((IGGAPIKeyRealm) object).getAlgo()) &&
					this.getName().equals(((IGGAPIKeyRealm) object).getName()) && 
					this.getCipheringKey().equals(((IGGAPIKeyRealm) object).getCipheringKey()) &&
					this.getUncipheringKey().equals(((IGGAPIKeyRealm) object).getUncipheringKey());
		} catch (GGAPIKeyExpiredException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean equals(IGGAPIKeyRealm object) {
		try {
			return this.getAlgo().equals(((IGGAPIKeyRealm) object).getAlgo()) &&
					this.getName().equals(((IGGAPIKeyRealm) object).getName()) && 
					this.getCipheringKey().equals(((IGGAPIKeyRealm) object).getCipheringKey()) &&
					this.getUncipheringKey().equals(((IGGAPIKeyRealm) object).getUncipheringKey());
		} catch (GGAPIKeyExpiredException e) {
			e.printStackTrace();
			return false;
		}
	}

}
