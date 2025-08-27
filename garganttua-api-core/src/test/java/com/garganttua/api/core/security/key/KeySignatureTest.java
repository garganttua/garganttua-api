package com.garganttua.api.core.security.key;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKey;

public class KeySignatureTest {
	
	@Test
	public void testSignatureSHA224withRSA() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.RSA_4096, null, SignatureAlgorithm.SHA224);
		
		IKey signingKey = realm.getKeyForSigning();
		IKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}
	
	@Test
	public void testSignatureSHA256withECDSA() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.EC_256, null, SignatureAlgorithm.SHA256);
		
		IKey signingKey = realm.getKeyForSigning();
		IKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}
	
	@Test
	public void testSignatureSHA1withRSA() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.RSA_4096, null, SignatureAlgorithm.SHA1);
		
		IKey signingKey = realm.getKeyForSigning();
		IKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}
	
	@Test
	public void testSignatureSHA256withDSA() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.DSA_2048, null, SignatureAlgorithm.SHA256);
		
		IKey signingKey = realm.getKeyForSigning();
		IKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}
	
	@Test
	public void testSignatureMD5withRSA() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.RSA_3072, null, SignatureAlgorithm.MD5);
		
		IKey signingKey = realm.getKeyForSigning();
		IKey verifiingKey = realm.getKeyForSignatureVerification();
		
		byte[] signature = signingKey.sign("Salut".getBytes());
		boolean signatureOk = verifiingKey.verifySignature(signature, "Salut".getBytes());
		
		assertTrue(signatureOk);
	}

}
