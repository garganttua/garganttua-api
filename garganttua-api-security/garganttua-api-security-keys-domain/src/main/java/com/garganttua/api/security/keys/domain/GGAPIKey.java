package com.garganttua.api.security.keys.domain;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.security.IGGAPIKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GGAPIKey implements IGGAPIKey {
	
	private GGAPIKeyType type;
	
	private String algorithm;
	
	private byte[] key;

	@Override
	public boolean equals(Object obj) {
		return Arrays.equals(key, ((GGAPIKey) obj).key);
	}

	@Override
	public Key getSigningKey() throws GGAPISecurityException {
		Key key_ = null;
		try {
			if( this.type == GGAPIKeyType.SECRET ) {
				key_ = new SecretKeySpec(this.key, 0, this.key.length, this.algorithm);
			}
			if( this.type == GGAPIKeyType.PRIVATE ) {
					key_ = KeyFactory.getInstance(this.algorithm).generatePrivate(new PKCS8EncodedKeySpec(this.key));
			}
			if( this.type == GGAPIKeyType.PUBLIC ) {
				key_ = KeyFactory.getInstance(this.algorithm).generatePublic(new PKCS8EncodedKeySpec(this.key));
			}
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new GGAPISecurityException(e);
		}
		return key_;
	}
}
