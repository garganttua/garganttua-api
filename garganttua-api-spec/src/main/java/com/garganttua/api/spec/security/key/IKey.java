package com.garganttua.api.spec.security.key;

import java.security.Key;

import com.garganttua.core.CoreException;

public interface IKey {
	
	byte[] sign(byte[] data) throws CoreException;

	boolean verifySignature(byte[] signature, byte[] originalData) throws CoreException;
	
	byte[] encrypt(byte[] clear) throws CoreException;
	
	byte[] decrypt(byte[] encoded) throws CoreException;
	
	/**
	 * Base64 encoded key
	 * @return
	 */
	byte[] getRawKey();

	Key getKey() throws CoreException;

	KeyType getType();

	KeyAlgorithm getAlgorithm();

	byte[] getInitializationVector();

	EncryptionMode getEncryptionMode();

	EncryptionPaddingMode getEncryptionPaddingMode();

	SignatureAlgorithm getSignatureAlgorithm();

}
