package com.garganttua.api.core.security.key;

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

}
