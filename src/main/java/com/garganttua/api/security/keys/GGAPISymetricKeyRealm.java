package com.garganttua.api.security.keys;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class GGAPISymetricKeyRealm implements IGGAPIKeyRealm {

	private Date expirationDate = null;
	private SecretKey secret;

	public GGAPISymetricKeyRealm(SignatureAlgorithm algo, GGAPIKeyExpiration expiration) {
		if( expiration != null ) {
			this.expirationDate = new Date(System.currentTimeMillis() + expiration.unit().toMillis(expiration.time()));
		}
		this.secret = Keys.secretKeyFor(algo);
	}

	@Override
	public Key getCipheringKey() throws GGAPIKeyExpiredException {
		if( this.expirationDate != null ) {
			if( new Date().after(this.expirationDate) ) {
				throw new GGAPIKeyExpiredException("The key has expired");
			}
		}
		
		return this.secret;
	}

	@Override
	public Key getUncipheringKey() throws GGAPIKeyExpiredException {
		if( this.expirationDate != null ) {
			if( new Date().after(this.expirationDate) ) {
				throw new GGAPIKeyExpiredException("The key has expired");
			}
		}
		
		return this.secret;
	}

}
