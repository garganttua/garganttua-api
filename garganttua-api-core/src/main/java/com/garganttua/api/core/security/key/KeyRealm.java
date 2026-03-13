package com.garganttua.api.core.security.key;

import java.security.KeyPair;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.EntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.EntityId;
import com.garganttua.api.spec.entity.annotations.EntityMandatory;
import com.garganttua.api.spec.entity.annotations.EntityOwned;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;
import com.garganttua.api.spec.entity.annotations.EntityUuid;
import com.garganttua.api.spec.security.SecurityRandoms;
import com.garganttua.api.spec.security.key.EncryptionMode;
import com.garganttua.api.spec.security.key.EncryptionPaddingMode;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.KeyType;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKey;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;

import lombok.Getter;

@EntityOwned(ownerId = "ownerId")
public class KeyRealm implements IKeyRealm {

	// Ctr for encryption only

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, null, -1, encryptionMode, paddingMode, null);
	}

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, expiration, -1, encryptionMode, paddingMode, null);
	}

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, int initializationVectorSize,
			EncryptionMode encryptionMode, EncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, null, initializationVectorSize, encryptionMode, paddingMode, null);
	}

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, int initializationVectorSize,
			EncryptionMode encryptionMode, EncryptionPaddingMode paddingMode) {
		this(keyRealmName, keyAlgorithm, expiration, initializationVectorSize, encryptionMode, paddingMode, null);
	}

	// Ctr for signature only

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, -1, null, null, signatureAlgorithm);
	}

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration,
			SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, expiration, -1, null, null, signatureAlgorithm);
	}

	// Ctr for signature and encryption

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, -1, encryptionMode, paddingMode, signatureAlgorithm);
	}

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, int initializationVectorSize,
			EncryptionMode encryptionMode, EncryptionPaddingMode paddingMode,
			SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, null, initializationVectorSize, encryptionMode, paddingMode,
				signatureAlgorithm);
	}

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) {
		this(keyRealmName, keyAlgorithm, expiration, -1, encryptionMode, paddingMode, signatureAlgorithm);
	}

	// Complete CTR

	public KeyRealm(String keyRealmName, KeyAlgorithm keyAlgorithm, Date expiration, int initializationVectorSize,
			EncryptionMode encryptionMode, EncryptionPaddingMode paddingMode,
			SignatureAlgorithm signatureAlgorithm) {
		this.id = keyRealmName;
		this.keyAlgorithm = keyAlgorithm;
		this.expiration = expiration;
		if (keyAlgorithm != null) {
			var realmType = keyAlgorithm.getType();
			this.keyType = (realmType == com.garganttua.api.spec.security.key.KeyRealmType.SYMETRIC)
					? KeyType.SECRET : KeyType.PRIVATE;
		}
		if (initializationVectorSize > 0) {
			this.initializationVector = new byte[initializationVectorSize];
			SecurityRandoms.secureRandom().nextBytes(this.initializationVector);
		}
		this.encryptionMode = encryptionMode;
		this.paddingMode = paddingMode;
		this.signatureAlgorithm = signatureAlgorithm;
		if (keyAlgorithm != null)
			this.createKeys();
	}

	@EntityUuid
	@Getter
	protected String uuid;

	@EntityId
	@Getter
	protected String id;

	@EntityOwnerId
	@Getter
	protected String ownerId;

	protected byte[] initializationVector;

	protected EncryptionMode encryptionMode;

	protected EncryptionPaddingMode paddingMode;

	@Getter
	protected SignatureAlgorithm signatureAlgorithm;

	@EntityMandatory
	@Getter
	protected KeyAlgorithm keyAlgorithm;

	protected KeyType keyType;

	@JsonProperty
	protected Key encryptionKey;

	@JsonProperty
	protected Key decryptionKey;

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
		if (this.keyAlgorithm.getType() == com.garganttua.api.spec.security.key.KeyRealmType.SYMETRIC) {
			SecretKey key = this.keyAlgorithm.generateSymetricKey();
			this.encryptionKey = new Key(KeyType.SECRET, this.keyAlgorithm, key.getEncoded(),
					this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
			this.decryptionKey = new Key(KeyType.SECRET, this.keyAlgorithm, key.getEncoded(),
					this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
		} else {
			KeyPair keyPair = this.keyAlgorithm.generateAsymetricKey();
			this.encryptionKey = new Key(KeyType.PRIVATE, this.keyAlgorithm, keyPair.getPrivate().getEncoded(),
					this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
			this.decryptionKey = new Key(KeyType.PUBLIC, this.keyAlgorithm, keyPair.getPublic().getEncoded(),
					this.initializationVector, this.encryptionMode, this.paddingMode, this.signatureAlgorithm);
		}
	}

	@Override
	public String getName() {
		return this.id;
	}

	@Override
	public boolean equals(IKeyRealm object) {
		return false;
	}

	@Override
	@JsonIgnore
	public IKey getKeyForSigning() throws ApiException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.encryptionKey;
	}

	@Override
	@JsonIgnore
	public IKey getKeyForSignatureVerification() throws ApiException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.decryptionKey;
	}

	@Override
	@JsonIgnore
	public IKey getKeyForEncryption() throws ApiException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.encryptionKey;
	}

	private void throwExceptionIfRevoked() throws SecurityException {
		if (this.revoked) {
			throw new SecurityException(CoreExceptionCode.KEY_REVOKED,
					"The key for realm " + this.id + " has been revoked");
		}
	}

	private void throwExceptionIfExpired() throws SecurityException {
		if (this.expiration != null && new Date().after(this.expiration)) {
			throw new SecurityException(CoreExceptionCode.KEY_EXPIRED,
					"The key for realm " + this.id + " has expired");
		}
	}

	@Override
	@JsonIgnore
	public IKey getKeyForDecryption() throws ApiException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.decryptionKey;
	}

	public static ObjectAddress getExpirationFieldAddress() {
		try {
			return new ObjectAddress("expiration");
		} catch (ReflectionException e) {
			return null;
		}
	}

	public static ObjectAddress getRevokedFieldAddress() {
		try {
			return new ObjectAddress("revoked");
		} catch (ReflectionException e) {
			return null;
		}
	}

	public static ObjectAddress getOwnerIdFieldAddress() {
		try {
			return new ObjectAddress("ownerId");
		} catch (ReflectionException e) {
			return null;
		}
	}

	public static ObjectAddress getAlgorithmFieldAddress() {
		try {
			return new ObjectAddress("algorithm");
		} catch (ReflectionException e) {
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

	// IKey-like methods delegated to encryptionKey

	@Override
	public byte[] sign(byte[] data) throws ApiException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.encryptionKey.sign(data);
	}

	@Override
	public boolean verifySignature(byte[] signature, byte[] originalData) throws ApiException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.decryptionKey.verifySignature(signature, originalData);
	}

	@Override
	public byte[] encrypt(byte[] clear) throws ApiException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.encryptionKey.encrypt(clear);
	}

	@Override
	public byte[] decrypt(byte[] encoded) throws ApiException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.decryptionKey.decrypt(encoded);
	}

	@Override
	public byte[] getRawKey() {
		return this.encryptionKey != null ? this.encryptionKey.getRawKey() : null;
	}

	@Override
	@JsonIgnore
	public java.security.Key getKey() throws ApiException {
		return this.encryptionKey != null ? this.encryptionKey.getKey() : null;
	}

	@Override
	public KeyType getType() {
		return this.keyType;
	}

	@Override
	public KeyAlgorithm getAlgorithm() {
		return this.keyAlgorithm;
	}

	@Override
	public byte[] getInitializationVector() {
		return this.initializationVector;
	}

	@Override
	public EncryptionMode getEncryptionMode() {
		return this.encryptionMode;
	}

	@Override
	public EncryptionPaddingMode getEncryptionPaddingMode() {
		return this.paddingMode;
	}
}
