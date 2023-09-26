package com.garganttua.api.security.keys;

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
	private SignatureAlgorithm algo;
	private String realm;

	public GGAPIAsymetricKeyRealm(String realm, SignatureAlgorithm algo, GGAPIKeyExpiration expiration) {
		super(algo, expiration);
		this.realm = realm;
		this.algo = algo;
		if( expiration != null ) {
			this.pub.setExpiration(new Date(System.currentTimeMillis() + expiration.unit().toMillis(expiration.time())));
			this.pri.setExpiration(new Date(System.currentTimeMillis() + expiration.unit().toMillis(expiration.time())));
		}
	}

	@Override
	protected void getKey(SignatureAlgorithm algo) {
		KeyPair keys = Keys.keyPairFor(algo);
		PrivateKey private__ = keys.getPrivate();
		this.pri = new GGAPIKey(UUID.randomUUID().toString(), this.realm, private__.getAlgorithm(), null, GGAPIKeyType.PRIVATE, private__.getEncoded());
		PublicKey public__ = keys.getPublic();
		this.pub = new GGAPIKey(UUID.randomUUID().toString(), this.realm, public__.getAlgorithm(), null, GGAPIKeyType.PRIVATE, public__.getEncoded());
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
	
}
