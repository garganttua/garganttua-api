package com.garganttua.api.core.security.keys;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class GGAPIAsymetricKeyRealm extends AbstractGGAPIKeyRealm {

	private GGAPIKey pub;
	private GGAPIKey pri;
	private String algo;
	private String realm;

	public GGAPIAsymetricKeyRealm(String realm, String algo, GGAPIKey pub, GGAPIKey pri) {
		super();
		this.realm = realm;
		this.algo = algo;
		this.pub = pub;
		this.pri = pri;
	}

	public GGAPIAsymetricKeyRealm(String realm, String algo, GGAPIKeyExpiration expiration) {
		super(algo, expiration);
		this.realm = realm;
		this.algo = algo;
		if (expiration != null) {
			this.pub.setExpiration(
					new Date(System.currentTimeMillis() + expiration.unit().toMillis(expiration.time())));
			this.pri.setExpiration(
					new Date(System.currentTimeMillis() + expiration.unit().toMillis(expiration.time())));
		}
	}

	@Override
	protected void getKey(String algo) {
		KeyPair keys = Keys.keyPairFor(SignatureAlgorithm.forName(algo));
		PrivateKey private__ = keys.getPrivate();
		this.pri = new GGAPIKey(UUID.randomUUID().toString(), private__.getAlgorithm(), null,
				GGAPIKeyType.PRIVATE, private__.getEncoded());
		PublicKey public__ = keys.getPublic();
		this.pub = new GGAPIKey(UUID.randomUUID().toString(), public__.getAlgorithm(), null,
				GGAPIKeyType.PUBLIC, public__.getEncoded());
	}

	@Override
	protected GGAPIKey getCipheringKey_() {
		return this.pri;
	}

	@Override
	protected GGAPIKey getUncipheringKey_() {
		return this.pub;
	}

	@Override
	public String getName() {
		return this.realm;
	}

	@Override
	public String getAlgo() {
		return this.algo.toString();
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
