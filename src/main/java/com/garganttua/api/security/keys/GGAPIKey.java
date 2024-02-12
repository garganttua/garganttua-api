package com.garganttua.api.security.keys;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class GGAPIKey {
	
	@Id
	@Indexed(unique=true)
	private String uuid;
	
	@Field
	private String algorithm;
	
	@Field
	private Date expiration;
	
	@Field
	private GGAPIKeyType type;
	
	@Field
	@JsonIgnore
	private byte[] key;

	@JsonIgnore
	public Key getKeyForCiphering() throws InvalidKeySpecException, NoSuchAlgorithmException {
		Key key_ = null;
		if( this.type == GGAPIKeyType.SYMETRIC ) {
			key_ = new SecretKeySpec(key, 0, key.length, this.algorithm);
		}
		if( this.type == GGAPIKeyType.PRIVATE ) {
			key_ = KeyFactory.getInstance(this.algorithm).generatePrivate(new PKCS8EncodedKeySpec(this.key));
		}
		if( this.type == GGAPIKeyType.PUBLIC ) {
			key_ = KeyFactory.getInstance(this.algorithm).generatePublic(new X509EncodedKeySpec(this.key));
		}
		return key_;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean date = true;
		if( this.expiration == null ) {
			if( ((GGAPIKey) obj).expiration != null ) {
				date = false;
			}
		} else {
			if( ((GGAPIKey) obj).expiration == null ) {
				date = false;
			}
		}
		
		return date && this.type == ((GGAPIKey) obj).type &&
				this.algorithm.equals(((GGAPIKey) obj).algorithm);
	}
}
