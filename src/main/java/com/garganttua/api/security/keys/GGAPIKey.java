package com.garganttua.api.security.keys;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class GGAPIKey {
	
	private String uuid;
	
	private String realm;
	
	private String algorithm;
	
	private Date expiration;
	
	private GGAPIKeyType type;
	
	private byte[] encoded;

	public Key getKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
		Key key = null;
		if( this.type == GGAPIKeyType.SYMETRIC ) {
			key = new SecretKeySpec(encoded, 0, encoded.length, this.algorithm);
		}
		if( this.type == GGAPIKeyType.PRIVATE ) {
			key = KeyFactory.getInstance(this.algorithm).generatePrivate(new PKCS8EncodedKeySpec(this.encoded));
		}
		if( this.type == GGAPIKeyType.PUBLIC ) {
			key = KeyFactory.getInstance(this.algorithm).generatePublic(new X509EncodedKeySpec(this.encoded));
		}
		return key;
	}
}
