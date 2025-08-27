package com.garganttua.api.core.security.key;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.key.EncryptionMode;
import com.garganttua.api.spec.security.key.EncryptionPaddingMode;
import com.garganttua.api.spec.security.key.KeyAlgorithm;

public class KeyRealmEncryptDecryptTest {
	
	@Test
	public void testEncryptDecryptRSA4096_ECB_PKCS1_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.RSA_4096, null, EncryptionMode.ECB, EncryptionPaddingMode.PKCS1_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_ECB_PKCS5_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.AES_256, null, EncryptionMode.ECB, EncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_CBC_PKCS5_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.AES_256, null, 16, EncryptionMode.CBC, EncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_GCM_NO_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.AES_256, null, 12, EncryptionMode.GCM, EncryptionPaddingMode.NO_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_CTR_NO_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.AES_256, null, 16, EncryptionMode.CTR, EncryptionPaddingMode.NO_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptAES256_CFB_NO_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.AES_256, null, 16, EncryptionMode.CFB, EncryptionPaddingMode.NO_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
//	@Test
	public void testEncryptDecryptRSA512_NONE_NO_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.RSA_512, EncryptionMode.NONE, EncryptionPaddingMode.NO_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
//	@Test
	public void testEncryptDecryptEC384_ECDSA_NO_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.EC_384, EncryptionMode.ECDSA, EncryptionPaddingMode.NONE);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
//	@Test
	public void testEncryptDecryptDH1024_NONE_NO_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.DH_1024, EncryptionMode.NONE, EncryptionPaddingMode.NONE);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecrypt3DES168_CBC_PKCS1_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.DESEDE_168, 8, EncryptionMode.CBC, EncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptDES56_CBC_PKCS1_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.DES_56, 8, EncryptionMode.CBC, EncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}
	
	@Test
	public void testEncryptDecryptBLOWFISH120_CBC_PKCS5_PADDING() throws CoreException {
		KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.BLOWFISH_120, 8, EncryptionMode.CBC, EncryptionPaddingMode.PKCS5_PADDING);
		
		byte[] encryptWithPrivate = realm.getKeyForEncryption().encrypt("salut".getBytes());
		byte[] encryptWithPublic = realm.getKeyForDecryption().encrypt("salut".getBytes());
		
		byte[] decryptWithPrivate = realm.getKeyForEncryption().decrypt(encryptWithPublic);
		byte[] decryptWithPublic = realm.getKeyForDecryption().decrypt(encryptWithPrivate);
		
		assertEquals("salut", new String(decryptWithPrivate));
		assertEquals("salut", new String(decryptWithPublic));
	}

}
