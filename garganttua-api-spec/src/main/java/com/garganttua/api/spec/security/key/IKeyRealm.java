package com.garganttua.api.spec.security.key;

import com.garganttua.api.spec.CoreException;

public interface IKeyRealm {

	String getName();
	
	KeyAlgorithm getKeyAlgorithm();

	SignatureAlgorithm getSignatureAlgorithm();
	
	boolean equals(IKeyRealm object);

	/**
	 * Actually returns a public key, or a secret key
	 * @return
	 * @throws CoreException
	 */
	IKey getKeyForDecryption() throws CoreException;

	/**
	 * Actually returns a private key, or a secret key
	 * @return
	 * @throws CoreException
	 */
	IKey getKeyForEncryption() throws CoreException;
	
	/**
	 * Returns a private key for signing
	 * @return
	 * @throws CoreException
	 */
	IKey getKeyForSigning() throws CoreException;
	
	/**
	 * Returns a public key for signature verification
	 * @return
	 * @throws CoreException
	 */
	IKey getKeyForSignatureVerification() throws CoreException;

	String getUuid();

	void revoke();

	void removeKeyForEncryption();

    boolean isAbleToSign();

}
