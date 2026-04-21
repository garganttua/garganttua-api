package com.garganttua.api.commons.security.key;

import java.security.KeyPair;

import javax.crypto.SecretKey;

public interface IKeyAlgorithm {

	KeyRealmType getType() throws IllegalArgumentException;

	SecretKey generateSymetricKey() throws IllegalArgumentException;

	KeyPair generateAsymetricKey() throws IllegalArgumentException;

	String geCipherName(EncryptionMode mode, EncryptionPaddingMode padding) throws IllegalArgumentException;

	String geSignatureName(SignatureAlgorithm signatureAlgorithm);

}
