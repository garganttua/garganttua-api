package com.garganttua.api.spec.security.key;

import java.security.KeyPair;

import javax.crypto.SecretKey;

public interface IGGAPIKeyAlgorithm {

	GGAPIKeyRealmType getType() throws IllegalArgumentException;

	SecretKey generateSymetricKey() throws IllegalArgumentException;

	KeyPair generateAsymetricKey() throws IllegalArgumentException;

	String geCipherName(GGAPIEncryptionMode mode, GGAPIEncryptionPaddingMode padding) throws IllegalArgumentException;

	String geSignatureName(GGAPISignatureAlgorithm signatureAlgorithm);

}
