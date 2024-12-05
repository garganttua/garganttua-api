package com.garganttua.api.spec.security.key;

import java.security.Key;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIKey {
	
	byte[] sign(byte[] data) throws GGAPIException;

	boolean verifySignature(byte[] signature, byte[] originalData) throws GGAPIException;
	
	byte[] encrypt(byte[] clear) throws GGAPIException;
	
	byte[] decrypt(byte[] encoded) throws GGAPIException;
	
	/**
	 * Base64 encoded key
	 * @return
	 */
	byte[] getRawKey();

	Key getKey() throws GGAPIException;

	GGAPIKeyType getType();

	GGAPIKeyAlgorithm getAlgorithm();

	byte[] getInitializationVector();

	GGAPIEncryptionMode getEncryptionMode();

	GGAPIEncryptionPaddingMode getEncryptionPaddingMode();

	GGAPISignatureAlgorithm getSignatureAlgorithm();

}
