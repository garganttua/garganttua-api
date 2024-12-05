package com.garganttua.api.core.security.key;

import java.security.KeyPair;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.security.GGAPISecurityRandoms;
import com.garganttua.api.spec.security.key.GGAPIEncryptionMode;
import com.garganttua.api.spec.security.key.GGAPIEncryptionPaddingMode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPIKeyRealmType;
import com.garganttua.api.spec.security.key.GGAPIKeyType;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKey;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;

import lombok.Getter;

@GGAPIEntityOwned(ownerId = "ownerId")
public class GGAPIKeyRealm extends GenericGGAPIEntity implements IGGAPIKeyRealm {
		
	//Ctr for encryption only

	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, null, -1, encryptionMode, paddingMode, null);
	}
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, Date expiration, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, expiration, -1, encryptionMode, paddingMode, null);
	}	
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm,  int initializationVectorSize, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, null, initializationVectorSize, encryptionMode, paddingMode, null);
	}
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, Date expiration, int initializationVectorSize, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, expiration, initializationVectorSize, encryptionMode, paddingMode, null);
	}
	
	//Ctr for signature only
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, GGAPISignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, -1, null, null, signatureAlgorithm);
	}
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, Date expiration, GGAPISignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, expiration, -1, null, null, signatureAlgorithm);
	}
	
	//Ctr for signature and encryption
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode, GGAPISignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, -1, encryptionMode, paddingMode, signatureAlgorithm);
	}
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, int initializationVectorSize, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode, GGAPISignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, initializationVectorSize, encryptionMode, paddingMode, signatureAlgorithm);
	}
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, Date expiration, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode, GGAPISignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, expiration, -1, encryptionMode, paddingMode, signatureAlgorithm);
	}
	
//	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, Date expiration, int initializationVectorSize, GGAPIEncryptionMode encryptionMode,
//			GGAPIEncryptionPaddingMode paddingMode, GGAPISignatureAlgorithm signatureAlgorithm) {
//		this(keyRealmName, keyAlgorithm, expiration, initializationVectorSize, encryptionMode, paddingMode, signatureAlgorithm);
//	}
	
	//Complete CTR

	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm keyAlgorithm, Date expiration, int initializationVectorSize, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode, GGAPISignatureAlgorithm signatureAlgorithm) {
		super(null, keyRealmName);
		
		this.keyAlgorithm = keyAlgorithm;
		this.expiration = expiration;
		if( keyAlgorithm != null )
			this.type = keyAlgorithm.getType();
		if( initializationVectorSize > 0 ) {
			this.initializationVector = new byte[initializationVectorSize];
			GGAPISecurityRandoms.secureRandom().nextBytes(this.initializationVector);
		}

		this.encryptionMode = encryptionMode;
		this.paddingMode = paddingMode;
		this.signatureAlgorithm = signatureAlgorithm;
		if( keyAlgorithm != null )
			this.createKeys();
	}
	
	protected byte[] initializationVector;

	protected GGAPIEncryptionMode encryptionMode;

	protected GGAPIEncryptionPaddingMode paddingMode;

	protected GGAPISignatureAlgorithm signatureAlgorithm;

	@GGAPIEntityMandatory
	@Getter
	protected GGAPIKeyAlgorithm keyAlgorithm;
	
	@Getter
	protected GGAPIKeyRealmType type;
	
	/**
	 * Actually, a private key, or a secret key
	 */
	@JsonProperty
	protected GGAPIKey encryptionKey;
	
	/**
	 * Actually, a public key, or a secret key
	 */
	@JsonProperty
	protected GGAPIKey decryptionKey;
	
	@Getter
	protected String ownerId;
	
	@Getter
	protected Date expiration;
	
	@Getter
	@GGAPIEntityAuthorizeUpdate()
	protected boolean revoked;
	
	@GGAPIEntityBeforeCreate
	public void beforeCreate(IGGAPICaller caller, Map<String, String> params) throws GGAPISecurityException {
		this.createKeys();
	}
	
	@JsonIgnore
	private void createKeys() {
		if( this.type == GGAPIKeyRealmType.SYMETRIC) {
			SecretKey key = this.keyAlgorithm.generateSymetricKey();
			this.encryptionKey = new GGAPIKey(GGAPIKeyType.SECRET, this.keyAlgorithm, key.getEncoded(), this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
			this.decryptionKey = new GGAPIKey(GGAPIKeyType.SECRET, this.keyAlgorithm, key.getEncoded(), this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
		} else {
			KeyPair keyPair = this.keyAlgorithm.generateAsymetricKey();
			this.encryptionKey = new GGAPIKey(GGAPIKeyType.PRIVATE, this.keyAlgorithm, keyPair.getPrivate().getEncoded(), this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
			this.decryptionKey = new GGAPIKey(GGAPIKeyType.PUBLIC, this.keyAlgorithm, keyPair.getPublic().getEncoded(), this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
		}
	}

	@Override
	public String getName() {
		return this.id;
	}
	
	@Override
	public String getUuid() {
		return this.uuid;
	}

	@Override
	public boolean equals(IGGAPIKeyRealm object) {
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	@JsonIgnore
	public IGGAPIKey getKeyForSigning() throws GGAPIException {
    	this.throwExceptionIfExpired();
    	this.throwExceptionIfRevoked();
		return this.encryptionKey;
	}

	@Override
	@JsonIgnore
	public IGGAPIKey getKeyForSignatureVerification() throws GGAPIException {
    	this.throwExceptionIfExpired();
    	this.throwExceptionIfRevoked();
		return this.decryptionKey;
	}

    @Override
	@JsonIgnore
    public IGGAPIKey getKeyForEncryption() throws GGAPISecurityException {
    	this.throwExceptionIfExpired();
    	this.throwExceptionIfRevoked();
		return this.encryptionKey;
	}
    
    private void throwExceptionIfRevoked() throws GGAPISecurityException {
    	if( this.revoked ) {
    		throw new GGAPISecurityException(GGAPIExceptionCode.KEY_REVOKED, "The key for realm "+this.id+" has expired");
    	}
	}

	private void throwExceptionIfExpired() throws GGAPISecurityException {
    	if( this.expiration != null && new Date().after(this.expiration) ) {
    		throw new GGAPISecurityException(GGAPIExceptionCode.KEY_EXPIRED, "The key for realm "+this.id+" has expired");
    	}
	}

    @Override
	@JsonIgnore
	public IGGAPIKey getKeyForDecryption() throws GGAPISecurityException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.decryptionKey;
    }

	public static GGObjectAddress getExpirationFieldAddress() {
		try {
			return new GGObjectAddress("expiration");
		} catch (GGReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static GGObjectAddress getRevokedFieldAddress() {
		try {
			return new GGObjectAddress("revoked");
		} catch (GGReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static GGObjectAddress getOwnerIdFieldAddress() {
		try {
			return new GGObjectAddress("ownerId");
		} catch (GGReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static GGObjectAddress getAlgorithmFieldAddress() {
		try {
			return new GGObjectAddress("algorithm");
		} catch (GGReflectionException e) {
			//Should never happen
			return null;
		}
	}

	@Override
	public void revoke() {
		this.revoked = true;
	}

	@Override
	public void removeKeyForEncryption() {
		this.encryptionKey = null;
	}

}
