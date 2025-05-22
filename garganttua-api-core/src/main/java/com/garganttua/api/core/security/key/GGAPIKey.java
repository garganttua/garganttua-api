package com.garganttua.api.core.security.key;

import java.lang.reflect.Array;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.key.GGAPIEncryptionMode;
import com.garganttua.api.spec.security.key.GGAPIEncryptionPaddingMode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPIKeyType;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@NoArgsConstructor
@Slf4j
public class GGAPIKey implements IGGAPIKey {

	@JsonProperty
	private GGAPIKeyType type;

	@JsonProperty
	private GGAPIKeyAlgorithm algorithm;

	/**
	 * Base64 Encoded
	 */
	@JsonProperty
	private byte[] rawKey;

	@JsonProperty
	private byte[] initializationVector;

	@JsonProperty
	private GGAPIEncryptionMode encryptionMode;

	@JsonProperty
	private GGAPIEncryptionPaddingMode encryptionPaddingMode;

	@JsonProperty
	private GGAPISignatureAlgorithm signatureAlgorithm;

	public GGAPIKey(GGAPIKeyType type, GGAPIKeyAlgorithm algorithm, byte[] rawKey, byte[] initializationVector,
			GGAPIEncryptionMode encryptionMode, GGAPIEncryptionPaddingMode paddingMode,
			GGAPISignatureAlgorithm signatureAlgorithm) {
		super();
		this.type = type;
		this.algorithm = algorithm;
		this.encryptionMode = encryptionMode;
		this.encryptionPaddingMode = paddingMode;
		this.signatureAlgorithm = signatureAlgorithm;
		Encoder b64Encoder = Base64.getEncoder();
		this.rawKey = b64Encoder.encode(rawKey);
		this.initializationVector = initializationVector;
	}

	@Override
	public boolean equals(Object obj) {
		return Arrays.equals(rawKey, ((GGAPIKey) obj).rawKey);
	}

	@Override
	@JsonIgnore
	public Key getKey() throws GGAPISecurityException {
		Key key_ = null;
		Decoder b64Decoder = Base64.getDecoder();
		byte[] decodedRawKey = b64Decoder.decode(this.rawKey);
		try {
			if (this.type == GGAPIKeyType.SECRET) {
				key_ = new SecretKeySpec(decodedRawKey, 0, decodedRawKey.length, this.algorithm.getAlgorithm());
			}
			if (this.type == GGAPIKeyType.PRIVATE) {
				key_ = KeyFactory.getInstance(this.algorithm.getAlgorithm())
						.generatePrivate(new PKCS8EncodedKeySpec(decodedRawKey));
			}
			if (this.type == GGAPIKeyType.PUBLIC) {
				key_ = KeyFactory.getInstance(this.algorithm.getAlgorithm())
						.generatePublic(new X509EncodedKeySpec(decodedRawKey));
			}
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			log.atWarn().log("Error in getting keys from bytes", e);
			throw new GGAPISecurityException(e);
		}
		return key_;
	}

	@Override
	public byte[] encrypt(byte[] clear)
			throws GGAPISecurityException {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(this.algorithm.geCipherName(this.encryptionMode, this.encryptionPaddingMode));
			if (this.initializationVector != null)
				if (this.encryptionMode == GGAPIEncryptionMode.GCM)
					cipher.init(Cipher.ENCRYPT_MODE, this.getKey(),
							new GCMParameterSpec(128, this.initializationVector));
				else
					cipher.init(Cipher.ENCRYPT_MODE, this.getKey(), new IvParameterSpec(this.initializationVector));
			else
				cipher.init(Cipher.ENCRYPT_MODE, this.getKey());
			return cipher.doFinal(clear);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | GGAPISecurityException | InvalidAlgorithmParameterException e) {
			log.atWarn().log("Encryption error", e);
			throw new GGAPISecurityException(e);
		}
	}

	@Override
	public byte[] decrypt(byte[] encoded)
			throws GGAPISecurityException {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(this.algorithm.geCipherName(this.encryptionMode, this.encryptionPaddingMode));
			if (this.initializationVector != null)
				if (this.encryptionMode == GGAPIEncryptionMode.GCM)
					cipher.init(Cipher.DECRYPT_MODE, this.getKey(),
							new GCMParameterSpec(128, this.initializationVector));
				else
					cipher.init(Cipher.DECRYPT_MODE, this.getKey(), new IvParameterSpec(this.initializationVector));
			else
				cipher.init(Cipher.DECRYPT_MODE, this.getKey());
			return cipher.doFinal(encoded);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | GGAPISecurityException | InvalidAlgorithmParameterException e) {
			log.atWarn().log("Decryption error", e);
			throw new GGAPISecurityException(e);
		}
	}

	@Override
	public byte[] sign(byte[] data) throws GGAPIException {
		byte[] signed = null;
		String signatureName = this.algorithm.geSignatureName(this.signatureAlgorithm);
		switch (this.type) {
			case SECRET:
				Mac mac;
				try {
					mac = Mac.getInstance(signatureName);
					mac.init(this.getKey());
					signed = mac.doFinal(data);
				} catch (NoSuchAlgorithmException | InvalidKeyException e) {
					log.atWarn().log("Signature error", e);
					throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Signature error", e);
				}
				break;
			case PRIVATE:
				try {
					Signature signature = Signature.getInstance(signatureName);
					signature.initSign((PrivateKey) this.getKey());
					signature.update(data);
					signed = signature.sign();
				} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
					log.atWarn().log("Signature error", e);
					throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Signature error", e);
				}
				break;
			case PUBLIC:
			default:
				throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Public key cannot sign");
		}

		return signed;
	}

	@Override
	public boolean verifySignature(byte[] signature, byte[] originalData)
			throws GGAPIException {

		String signatureName = this.algorithm.geSignatureName(this.signatureAlgorithm);

		switch (this.type) {
			case SECRET:
				Mac mac;
				try {
					mac = Mac.getInstance(signatureName);
					mac.init(this.getKey());
					byte[] signed = mac.doFinal(originalData);

					if (!Arrays.equals(signature, signed)) {
						log.atWarn().log("Signature verification error");
						throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH,
								"Signature verification error");
					}
				} catch (NoSuchAlgorithmException | InvalidKeyException e) {
					log.atWarn().log("Signature verification error", e);
					throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Signature error", e);
				}
				break;
			case PUBLIC:
				try {
					Signature signatureVerify = Signature
							.getInstance(signatureName);
					signatureVerify.initVerify((PublicKey) this.getKey());
					signatureVerify.update(originalData);
					signatureVerify.verify(signature);
				} catch (NoSuchAlgorithmException | InvalidKeyException e) {
					log.atWarn().log("Signature verification error", e);
					throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR,
							"Signature verification error",
							e);
				} catch (SignatureException e) {
					log.atWarn().log("Signature verification error", e);
					throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH,
							"Signature verification error",
							e);
				}
				break;
			case PRIVATE:
			default:
				throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR,
						"private key cannot verify");
		}
		return true;
	}

}
