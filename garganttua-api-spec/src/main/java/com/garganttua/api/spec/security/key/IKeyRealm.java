package com.garganttua.api.spec.security.key;

import java.security.Key;

import com.garganttua.api.spec.ApiException;

public interface IKeyRealm {

	String getName();
	
	KeyAlgorithm getKeyAlgorithm();

	SignatureAlgorithm getSignatureAlgorithm();
	
	boolean equals(IKeyRealm object);

	/**
	 * Actually returns a public key, or a secret key
	 * @return
	 * @throws ApiException
	 */
	IKey getKeyForDecryption() throws ApiException;

	/**
	 * Actually returns a private key, or a secret key
	 * @return
	 * @throws ApiException
	 */
	IKey getKeyForEncryption() throws ApiException;
	
	/**
	 * Returns a private key for signing
	 * @return
	 * @throws ApiException
	 */
	IKey getKeyForSigning() throws ApiException;
	
	/**
	 * Returns a public key for signature verification
	 * @return
	 * @throws ApiException
	 */
	IKey getKeyForSignatureVerification() throws ApiException;

	String getUuid();

	void revoke();

	void removeKeyForEncryption();

    boolean isAbleToSign();

	byte[] sign(byte[] data) throws ApiException;

	boolean verifySignature(byte[] signature, byte[] originalData) throws ApiException;
	
	byte[] encrypt(byte[] clear) throws ApiException;
	
	byte[] decrypt(byte[] encoded) throws ApiException;
	
	/**
	 * Base64 encoded key
	 * @return
	 */
	byte[] getRawKey();

	Key getKey() throws ApiException;

	KeyType getType();

	KeyAlgorithm getAlgorithm();

	byte[] getInitializationVector();

	EncryptionMode getEncryptionMode();

	EncryptionPaddingMode getEncryptionPaddingMode();

}
