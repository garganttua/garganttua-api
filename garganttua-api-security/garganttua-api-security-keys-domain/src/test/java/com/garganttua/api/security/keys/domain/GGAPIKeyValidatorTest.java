package com.garganttua.api.security.keys.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;

public class GGAPIKeyValidatorTest {

	@Test
	public void testGenerateSymetricKeys()
			throws GGAPISecurityException, NumberFormatException, NoSuchAlgorithmException {
		for (String algoSize : GGAPIKeyValidator.supportedAlgorithms) {
			String[] infos = GGAPIKeyValidator.validateAlgorithm(algoSize);

			if (GGAPIKeyValidator.determineAlgorithmType(algoSize) == GGAPIKeyType.SYMETRIC) {
				SecretKey key = GGAPIKeyValidator.generateSymetricKey(infos[0], Integer.valueOf(infos[1]));
				assertNotNull(key);
			}
		}
	}

	@Test
	public void testGenerateAsymetricKeys() throws GGAPISecurityException {
		for (String algoSize : GGAPIKeyValidator.supportedAlgorithms) {
			String[] infos = GGAPIKeyValidator.validateAlgorithm(algoSize);

			if (GGAPIKeyValidator.determineAlgorithmType(algoSize) == GGAPIKeyType.ASYMETRIC) {
				KeyPair keyPair = GGAPIKeyValidator.generateAsymetricKey(infos[0], Integer.valueOf(infos[1]));
				assertNotNull(keyPair);
			}
		}
	}
}
