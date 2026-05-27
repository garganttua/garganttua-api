package com.garganttua.api.core.security.key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKey;

public class GGAPIKeySignatureTest {
	
	@Test
	public void testSignatureSHA224withRSA() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.RSA_4096, null, GGAPISignatureAlgorithm.SHA224);
		
		IGGAPIKey signingKey = realm.getKeyForSigning();
		IGGAPIKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}
	
	@Test
	public void testSignatureSHA256withECDSA() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.EC_256, null, GGAPISignatureAlgorithm.SHA256);
		
		IGGAPIKey signingKey = realm.getKeyForSigning();
		IGGAPIKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}
	
	@Test
	public void testSignatureSHA1withRSA() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.RSA_4096, null, GGAPISignatureAlgorithm.SHA1);
		
		IGGAPIKey signingKey = realm.getKeyForSigning();
		IGGAPIKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}
	
	@Test
	public void testSignatureSHA256withDSA() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.DSA_2048, null, GGAPISignatureAlgorithm.SHA256);
		
		IGGAPIKey signingKey = realm.getKeyForSigning();
		IGGAPIKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}
	
	@Test
	public void testSignatureMD5withRSA() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.RSA_3072, null, GGAPISignatureAlgorithm.MD5);

		IGGAPIKey signingKey = realm.getKeyForSigning();
		IGGAPIKey verifiingKey = realm.getKeyForSignatureVerification();

		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());

		assertTrue(signatureOk);
	}

	// ───── HMAC paths (regression tests for 2.0.10 — pre-fix every one of
	// these failed with NoSuchAlgorithmException because geSignatureName
	// produced "<digest>withHmacSHA<size>" instead of the bare "HmacSHA<size>"
	// name JCE expects for Mac.getInstance) ─────

	@Test
	public void testSignatureHmacSHA512_explicitHmacSignatureAlgorithm() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.HMAC_SHA512_512, null,
				GGAPISignatureAlgorithm.HMAC_SHA512);

		IGGAPIKey signingKey = realm.getKeyForSigning();
		IGGAPIKey verifiingKey = realm.getKeyForSignatureVerification();

		byte[] signature = signingKey.sign("Salut".getBytes());
		assertTrue(verifiingKey.verifySignature(signature, "Salut".getBytes()),
				"HMAC sign+verify must round-trip when both key and signature algo are HMAC_SHA512");
	}

	@Test
	public void testSignatureHmacSHA512_withShaSignatureAlgorithm_doesNotProduceWithSuffix() throws GGAPIException {
		// This is the exact configuration that surfaced the bug at a 2.0.9
		// user (super-tenant login on a JWT-signed authorization wired with
		// HMAC key + SHA512 signature algorithm).
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.HMAC_SHA512_512, null,
				GGAPISignatureAlgorithm.SHA512);

		IGGAPIKey signingKey = realm.getKeyForSigning();
		IGGAPIKey verifiingKey = realm.getKeyForSignatureVerification();

		byte[] signature = signingKey.sign("Salut".getBytes());
		assertTrue(verifiingKey.verifySignature(signature, "Salut".getBytes()),
				"HMAC key paired with a SHA* signature algo must still round-trip — JCE's Mac requires the bare 'HmacSHA<size>' name");
	}

	@Test
	public void testSignatureHmacSHA256_withShaSignatureAlgorithm() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.HMAC_SHA256_256, null,
				GGAPISignatureAlgorithm.SHA256);

		IGGAPIKey signingKey = realm.getKeyForSigning();
		IGGAPIKey verifiingKey = realm.getKeyForSignatureVerification();

		byte[] signature = signingKey.sign("Salut".getBytes());
		assertTrue(verifiingKey.verifySignature(signature, "Salut".getBytes()));
	}

	@Test
	public void geSignatureName_onHmacKey_returnsBareKeyAlgorithmName_regardlessOfSignatureAlgo() {
		// Pin the geSignatureName contract directly: when the key algorithm
		// is HMAC, the returned signature name is the key algorithm name
		// itself (the JCE Mac name). Any "<digest>with<algo>" composition
		// would be rejected by Mac.getInstance() at runtime.
		assertEquals("HmacSHA512",
				GGAPIKeyAlgorithm.HMAC_SHA512_512.geSignatureName(GGAPISignatureAlgorithm.SHA512),
				"HMAC key + SHA512 signature algo must collapse to bare 'HmacSHA512'");
		assertEquals("HmacSHA256",
				GGAPIKeyAlgorithm.HMAC_SHA256_256.geSignatureName(GGAPISignatureAlgorithm.SHA256));
		// Non-HMAC keys keep the legacy "<digest>with<algo>" composition.
		assertEquals("SHA512withRSA",
				GGAPIKeyAlgorithm.RSA_4096.geSignatureName(GGAPISignatureAlgorithm.SHA512));
		assertEquals("SHA256withECDSA",
				GGAPIKeyAlgorithm.EC_256.geSignatureName(GGAPISignatureAlgorithm.SHA256));
	}

}
