package com.garganttua.api.core.legacy.security.key;

import java.security.KeyPair;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.legacy.entity.GenericEntity;
import com.garganttua.api.core.legacy.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.EntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.EntityMandatory;
import com.garganttua.api.spec.entity.annotations.EntityOwned;
import com.garganttua.api.spec.security.SecurityRandoms;
import com.garganttua.api.spec.security.key.EncryptionMode;
import com.garganttua.api.spec.security.key.EncryptionPaddingMode;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.KeyRealmType;
import com.garganttua.api.spec.security.key.KeyType;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKey;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;

import lombok.Getter;

@EntityOwned(ownerId = "ownerId")
public class KeyRealm extends GenericEntity implements IKeyRealm {
		
	//Ctr for encryption only

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, null, -1, encryptionMode, paddingMode, null);
	}
	
	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, expiration, -1, encryptionMode, paddingMode, null);
	}	
	
	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm,  int initializationVectorSize, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, null, initializationVectorSize, encryptionMode, paddingMode, null);
	}
	
	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, int initializationVectorSize, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, expiration, initializationVectorSize, encryptionMode, paddingMode, null);
	}
	
	//Ctr for signature only
	
	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, -1, null, null, signatureAlgorithm);
	}
	
	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, expiration, -1, null, null, signatureAlgorithm);
	}
	
	//Ctr for signature and encryption
	
	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, -1, encryptionMode, paddingMode, signatureAlgorithm);
	}
	
	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, int initializationVectorSize, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, initializationVectorSize, encryptionMode, paddingMode, signatureAlgorithm);
	}
	
	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, expiration, -1, encryptionMode, paddingMode, signatureAlgorithm);
	}

	//Complete CTR

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, int initializationVectorSize, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) {
		super(null, keyRealmName);
		
		this.keyAlgorithm = keyAlgorithm;
		this.expiration = expiration;
		if( keyAlgorithm != null )
			this.type = keyAlgorithm.getType();
		if( initializationVectorSize > 0 ) {
			this.initializationVector = new byte[initializationVectorSize];
			SecurityRandoms.secureRandom().nextBytes(this.initializationVector);
		}

		this.encryptionMode = encryptionMode;
		this.paddingMode = paddingMode;
		this.signatureAlgorithm = signatureAlgorithm;
		if( keyAlgorithm != null )
			this.createKeys();
	}
	
	protected byte[] initializationVector;

	protected EncryptionMode encryptionMode;

	protected EncryptionPaddingMode paddingMode;

	@Getter
	protected SignatureAlgorithm signatureAlgorithm;

	@EntityMandatory
	@Getter
	protected KeyAlgorithm keyAlgorithm;
	
	@Getter
	protected KeyRealmType type;
	
	/**
	 * Actually, a private key, or a secret key
	 */
	@JsonProperty
	protected Key encryptionKey;
	
	/**
	 * Actually, a public key, or a secret key
	 */
	@JsonProperty
	protected Key decryptionKey;
	
	@Getter
	protected String ownerId;
	
	@Getter
	protected Date expiration;
	
	@Getter
	@EntityAuthorizeUpdate()
	protected boolean revoked;
	
	@EntityBeforeCreate
	public void beforeCreate(ICaller caller, Map<String, String> params) throws SecurityException {
		this.createKeys();
	}
	
	@JsonIgnore
	private void createKeys() {
		if( this.type == KeyRealmType.SYMETRIC) {
			SecretKey key = this.keyAlgorithm.generateSymetricKey();
			this.encryptionKey = new Key(KeyType.SECRET, this.keyAlgorithm, key.getEncoded(), this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
			this.decryptionKey = new Key(KeyType.SECRET, this.keyAlgorithm, key.getEncoded(), this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
		} else {
			KeyPair keyPair = this.keyAlgorithm.generateAsymetricKey();
			this.encryptionKey = new Key(KeyType.PRIVATE, this.keyAlgorithm, keyPair.getPrivate().getEncoded(), this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
			this.decryptionKey = new Key(KeyType.PUBLIC, this.keyAlgorithm, keyPair.getPublic().getEncoded(), this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
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
	public boolean equals(IKeyRealm object) {
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	@JsonIgnore
	public IKey getKeyForSigning() throws CoreException {
    	this.throwExceptionIfExpired();
    	this.throwExceptionIfRevoked();
		return this.encryptionKey;
	}

	@Override
	@JsonIgnore
	public IKey getKeyForSignatureVerification() throws CoreException {
    	this.throwExceptionIfExpired();
    	this.throwExceptionIfRevoked();
		return this.decryptionKey;
	}

    @Override
	@JsonIgnore
    public IKey getKeyForEncryption() throws SecurityException {
    	this.throwExceptionIfExpired();
    	this.throwExceptionIfRevoked();
		return this.encryptionKey;
	}
    
    private void throwExceptionIfRevoked() throws SecurityException {
    	if( this.revoked ) {
    		throw new SecurityException(CoreExceptionCode.KEY_REVOKED, "The key for realm "+this.id+" has expired");
    	}
	}

	private void throwExceptionIfExpired() throws SecurityException {
    	if( this.expiration != null && new Date().after(this.expiration) ) {
    		throw new SecurityException(CoreExceptionCode.KEY_EXPIRED, "The key for realm "+this.id+" has expired");
    	}
	}

    @Override
	@JsonIgnore
	public IKey getKeyForDecryption() throws SecurityException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.decryptionKey;
    }

	public static ObjectAddress getExpirationFieldAddress() {
		try {
			return new ObjectAddress("expiration");
		} catch (ReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static ObjectAddress getRevokedFieldAddress() {
		try {
			return new ObjectAddress("revoked");
		} catch (ReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static ObjectAddress getOwnerIdFieldAddress() {
		try {
			return new ObjectAddress("ownerId");
		} catch (ReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static ObjectAddress getAlgorithmFieldAddress() {
		try {
			return new ObjectAddress("algorithm");
		} catch (ReflectionException e) {
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

	@Override
	public boolean isAbleToSign() {
		return this.signatureAlgorithm != null && this.keyAlgorithm != null;
	}
}
