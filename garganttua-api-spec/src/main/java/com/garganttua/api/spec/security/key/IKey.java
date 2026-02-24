package com.garganttua.api.spec.security.key;

import java.security.Key;

import com.garganttua.api.spec.ApiException;

public interface IKey {
	
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

	SignatureAlgorithm getSignatureAlgorithm();

}
