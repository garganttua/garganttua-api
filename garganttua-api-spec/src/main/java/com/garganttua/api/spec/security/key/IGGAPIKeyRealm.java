package com.garganttua.api.spec.security.key;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIKeyRealm {

	String getName();
	
	GGAPIKeyAlgorithm getKeyAlgorithm();

	GGAPISignatureAlgorithm getSignatureAlgorithm();
	
	boolean equals(IGGAPIKeyRealm object);

	/**
	 * Actually returns a public key, or a secret key
	 * @return
	 * @throws GGAPIException
	 */
	IGGAPIKey getKeyForDecryption() throws GGAPIException;

	/**
	 * Actually returns a private key, or a secret key
	 * @return
	 * @throws GGAPIException
	 */
	IGGAPIKey getKeyForEncryption() throws GGAPIException;
	
	/**
	 * Returns a private key for signing
	 * @return
	 * @throws GGAPIException
	 */
	IGGAPIKey getKeyForSigning() throws GGAPIException;
	
	/**
	 * Returns a public key for signature verification
	 * @return
	 * @throws GGAPIException
	 */
	IGGAPIKey getKeyForSignatureVerification() throws GGAPIException;

	String getUuid();

	void revoke();

	void removeKeyForEncryption();

    boolean isAbleToSign();

}
