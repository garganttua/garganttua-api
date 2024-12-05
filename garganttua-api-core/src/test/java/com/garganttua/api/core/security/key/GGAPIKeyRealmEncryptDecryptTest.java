package com.garganttua.api.core.security.key;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.key.GGAPIEncryptionMode;
import com.garganttua.api.spec.security.key.GGAPIEncryptionPaddingMode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;

public class GGAPIKeyRealmEncryptDecryptTest {
	
	@Test
	public void testEncryptDecryptRSA4096_ECB_PKCS1_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.RSA_4096, null, GGAPIEncryptionMode.ECB, GGAPIEncryptionPaddingMode.PKCS1_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_ECB_PKCS5_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.AES_256, null, GGAPIEncryptionMode.ECB, GGAPIEncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_CBC_PKCS5_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.AES_256, null, 16, GGAPIEncryptionMode.CBC, GGAPIEncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_GCM_NO_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.AES_256, null, 12, GGAPIEncryptionMode.GCM, GGAPIEncryptionPaddingMode.NO_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_CTR_NO_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.AES_256, null, 16, GGAPIEncryptionMode.CTR, GGAPIEncryptionPaddingMode.NO_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_CFB_NO_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.AES_256, null, 16, GGAPIEncryptionMode.CFB, GGAPIEncryptionPaddingMode.NO_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
//	@Test
	public void testEncryptDecryptRSA512_NONE_NO_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.RSA_512, GGAPIEncryptionMode.NONE, GGAPIEncryptionPaddingMode.NO_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
//	@Test
	public void testEncryptDecryptEC384_ECDSA_NO_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.EC_384, GGAPIEncryptionMode.ECDSA, GGAPIEncryptionPaddingMode.NONE);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
//	@Test
	public void testEncryptDecryptDH1024_NONE_NO_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.DH_1024, GGAPIEncryptionMode.NONE, GGAPIEncryptionPaddingMode.NONE);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecrypt3DES168_CBC_PKCS1_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.DESEDE_168, 8, GGAPIEncryptionMode.CBC, GGAPIEncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptDES56_CBC_PKCS1_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.DES_56, 8, GGAPIEncryptionMode.CBC, GGAPIEncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptBLOWFISH120_CBC_PKCS5_PADDING() throws GGAPIException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.BLOWFISH_120, 8, GGAPIEncryptionMode.CBC, GGAPIEncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}

}
