package com.garganttua.api.security.keys.domain;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUnicity;

import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class GGAPIKeyRealmEntity extends GenericGGAPIEntity implements IGGAPIKeyRealm {
	
	public static final String domain = "keys";
	
	@GGAPIEntityId
	@Setter
	@GGAPIEntityMandatory
	@GGAPIEntityUnicity
	protected String id;
	
	@GGAPIEntityMandatory
	@GGAPIEntityAuthorizeUpdate
	public String algorithm;
	
	public GGAPIKeyType type;

	private GGAPIKey cipheringKey;

	private GGAPIKey uncipheringKey;
	
	@GGAPIEntityAuthorizeUpdate
	public Date expiration;
	
	@GGAPIEntityAuthorizeUpdate
	public boolean revoked;
	
	@GGAPIEntityBeforeCreate
	private void createKeys(IGGAPICaller caller, Map<String, String> params) throws GGAPISecurityException {
		String[] infos = GGAPIKeyValidator.validateAlgorithm(this.algorithm);
		this.type = GGAPIKeyValidator.determineAlgorithmType(this.algorithm);
		
		if( this.type == GGAPIKeyType.SYMETRIC) {
			SecretKey key = GGAPIKeyValidator.generateSymetricKey(infos[0], Integer.valueOf(infos[1]));
			this.cipheringKey = new GGAPIKey(key.getEncoded());
			this.uncipheringKey = new GGAPIKey(key.getEncoded());
		} else {
			KeyPair keyPair = GGAPIKeyValidator.generateAsymetricKey(infos[0], Integer.valueOf(infos[1]));
			this.cipheringKey = new GGAPIKey(keyPair.getPrivate().getEncoded());
			this.uncipheringKey = new GGAPIKey(keyPair.getPublic().getEncoded());
		}
	}

	@Override
	public String getName() {
		return this.id;
	}

	@Override
	public boolean equals(IGGAPIKeyRealm object) {
		// TODO Auto-generated method stub
		return false;
	}

    
    public Key getKeyForCiphering() throws GGAPISecurityException {
    	this.throwExceptionIfExpired();
		Key key_ = null;
		if( this.type == GGAPIKeyType.SYMETRIC ) {
			key_ = new SecretKeySpec(this.cipheringKey.getKey(), 0, this.cipheringKey.getKey().length, this.algorithm);
		}
		if( this.type == GGAPIKeyType.ASYMETRIC ) {
			try {
				key_ = KeyFactory.getInstance(this.algorithm).generatePrivate(new PKCS8EncodedKeySpec(this.cipheringKey.getKey()));
			} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
				throw new GGAPISecurityException(e);
			}
		}
		return key_;
	}
    
    private void throwExceptionIfExpired() throws GGAPISecurityException {
    	if( this.expiration != null && new Date().after(this.expiration) ) {
    		throw new GGAPISecurityException(GGAPIExceptionCode.KEY_EXPIRED, "The key for realm "+this.id+" has expired");
    	}
	}

	public Key getKeyForUnciphering() throws GGAPISecurityException {
		this.throwExceptionIfExpired();
		Key key_ = null;
		if( this.type == GGAPIKeyType.SYMETRIC ) {
			key_ = new SecretKeySpec(this.uncipheringKey.getKey(), 0, this.uncipheringKey.getKey().length, this.algorithm);
		}
		if( this.type == GGAPIKeyType.ASYMETRIC ) {
			try {
				key_ = KeyFactory.getInstance(this.algorithm).generatePrivate(new PKCS8EncodedKeySpec(this.uncipheringKey.getKey()));
			} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
				throw new GGAPISecurityException(e);
			}
		}
		return key_;
    }

}
