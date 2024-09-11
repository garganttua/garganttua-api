package com.garganttua.api.security.keys.domain;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GGAPIKeyRealmEntity extends GenericGGAPIEntity implements IGGAPIKeyRealm {
	
	public static final String domain = "keys";
	
	@GGAPIEntityAuthorizeUpdate
	protected String algorithm = "RS512";
	
	protected GGAPIKeyType type;

	protected GGAPIKey cipheringKey;

	protected GGAPIKey uncipheringKey;
	
	@GGAPIEntityAuthorizeUpdate
	protected Date expiration;
	
	@GGAPIEntityAuthorizeUpdate
	protected boolean revoked;
	
	@GGAPIEntityBeforeCreate
	private void createKeys() throws GGAPISecurityException {

		
		this.createAsymetricKeys();
	}

	private void createAsymetricKeys() {
//		KeyPairGenerator.getInstance(algorithm);
//		
//		Mac key__ = Mac.getInstance("HmacSHA256");
//		key__.
		
//		Mac.getInstance("HmacSHA384");
//		Mac.getInstance("HmacSHA512");
		
		
		
//		KeyPair keys = Keys.keyPairFor(SignatureAlgorithm.forName(algo));
//		PrivateKey private__ = keys.getPrivate();
//		this.cipheringKey = new GGAPIKey(private__.getEncoded());
//		PublicKey public__ = keys.getPublic();
//		this.uncipheringKey = new GGAPIKey(public__.getEncoded());
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

	
//	switch (algo) {
//	default:
//	case HS256:
//	case HS384:
//	case HS512:
//		keyRealm = new GGAPISymetricKeyRealm(algo, expiration);
//		break;
//	case ES256:
//	case ES384:
//	case ES512:
//	case PS256:
//	case PS384:
//	case PS512:
//	case RS256:
//	case RS384:
//	case RS512:
//		break;
	
}
