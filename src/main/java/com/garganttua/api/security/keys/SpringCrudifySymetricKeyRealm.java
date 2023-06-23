package com.garganttua.api.security.keys;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class SpringCrudifySymetricKeyRealm implements ISpringCrudifyKeyRealm {

	private Date expirationDate = null;
	private SecretKey secret;

	public SpringCrudifySymetricKeyRealm(SignatureAlgorithm algo, SpringCrudifyKeyExpiration expiration) {
		if( expiration != null ) {
			this.expirationDate = new Date(System.currentTimeMillis() + expiration.unit().toMillis(expiration.time()));
		}
		this.secret = Keys.secretKeyFor(algo);
	}

	@Override
	public Key getCipheringKey() throws SpringCrudifyKeyExpiredException {
		if( this.expirationDate != null ) {
			if( new Date().after(this.expirationDate) ) {
				throw new SpringCrudifyKeyExpiredException("The key has expired");
			}
		}
		
		return this.secret;
	}

	@Override
	public Key getUncipheringKey() throws SpringCrudifyKeyExpiredException {
		if( this.expirationDate != null ) {
			if( new Date().after(this.expirationDate) ) {
				throw new SpringCrudifyKeyExpiredException("The key has expired");
			}
		}
		
		return this.secret;
	}

}
