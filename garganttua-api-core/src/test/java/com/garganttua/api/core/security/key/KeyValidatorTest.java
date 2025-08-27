package com.garganttua.api.core.security.key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.exceptions.SecurityException;

public class KeyValidatorTest {
//
//	@Test
//	public void testGenerateSymetricKeys()
//			throws SecurityException, NumberFormatException, NoSuchAlgorithmException {
//		for (String algoSize : KeyValidator.supportedAlgorithms) {
//			String[] infos = KeyValidator.validateAlgorithm(algoSize);
//
//			if (KeyValidator.determineAlgorithmType(algoSize) == KeyRealmType.SYMETRIC) {
//				SecretKey key = KeyValidator.generateSymetricKey(infos[0], Integer.valueOf(infos[1]));
//				assertNotNull(key);
//			}
//		}
//	}
//
//	@Test
//	public void testGenerateAsymetricKeys() throws SecurityException {
//		for (String algoSize : KeyValidator.supportedAlgorithms) {
//			String[] infos = KeyValidator.validateAlgorithm(algoSize);
//
//			if (KeyValidator.determineAlgorithmType(algoSize) == KeyRealmType.ASYMETRIC) {
//				KeyPair keyPair = KeyValidator.generateAsymetricKey(infos[0], Integer.valueOf(infos[1]));
//				assertNotNull(keyPair);
//			}
//		}
//	}
//	
//	@Test
//	public void test() throws SecurityException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
//		KeyPair keyPair = KeyValidator.generateAsymetricKey("RSA", 2048);
//		Key privateKey = new Key(KeyType.PRIVATE, keyPair.getPrivate().getAlgorithm(), keyPair.getPrivate().getEncoded());
//		Key publicKey = new Key(KeyType.PUBLIC, keyPair.getPublic().getAlgorithm(), keyPair.getPublic().getEncoded());
//
//		String data = "Exemple de texte";
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
//        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
//
//        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPublic());
//        byte[] decryptedData = cipher.doFinal(encryptedData);
//        
//        assertEquals(data, new String(decryptedData));
//		
//		byte[] encrypted = privateKey.cipher("un test".getBytes());
//		byte[] decrypted = publicKey.uncipher(encrypted);
//		
//		assertEquals("un test", new String(decrypted));
//		
//		byte[] encrypted2 = publicKey.cipher("un deuxième test".getBytes());
//		byte[] decrypted2 = privateKey.uncipher(encrypted2);
//		
//		assertEquals("un deuxième test", new String(decrypted2));
//	}
	
}
