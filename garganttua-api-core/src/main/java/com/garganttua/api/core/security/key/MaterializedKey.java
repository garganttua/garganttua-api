package com.garganttua.api.core.security.key;

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
import java.util.Objects;

import com.garganttua.core.crypto.CryptoException;
import com.garganttua.core.crypto.EncryptionMode;
import com.garganttua.core.crypto.EncryptionPaddingMode;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.KeyType;
import com.garganttua.core.crypto.SignatureAlgorithm;

/**
 * {@link IKey} built directly from key material bytes loaded from a persisted
 * key entity. Unlike the core-crypto {@code Key}/{@code KeyRealm} classes
 * which generate fresh material in their constructor, this class wraps
 * already-existing bytes — exactly what the {@code @Key} entity role
 * needs when looking up a previously stored asymmetric key.
 *
 * <p>Supports only the sign / verify path: the
 * {@code encrypt}/{@code decrypt} methods throw {@link CryptoException}.
 * That matches the current scope of the {@code @Key} entity role
 * (authorization signing). Extending to symmetric encryption is a
 * follow-up.
 */
public final class MaterializedKey implements IKey {

	private final KeyType type;
	private final IKeyAlgorithm algorithm;
	private final SignatureAlgorithm signatureAlgorithm;
	private final byte[] rawBytes;

	public MaterializedKey(KeyType type, IKeyAlgorithm algorithm, SignatureAlgorithm signatureAlgorithm, byte[] rawBytes) {
		this.type = Objects.requireNonNull(type, "type");
		this.algorithm = Objects.requireNonNull(algorithm, "algorithm");
		this.signatureAlgorithm = Objects.requireNonNull(signatureAlgorithm, "signatureAlgorithm");
		this.rawBytes = Objects.requireNonNull(rawBytes, "rawBytes").clone();
	}

	@Override
	public byte[] sign(byte[] data) throws CryptoException {
		if (this.type != KeyType.PRIVATE) {
			throw new CryptoException("sign requires a PRIVATE key, got " + this.type);
		}
		try {
			PrivateKey key = (PrivateKey) toJdkKey();
			Signature signer = Signature.getInstance(this.algorithm.getSignatureName(this.signatureAlgorithm));
			signer.initSign(key);
			signer.update(data);
			return signer.sign();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new CryptoException("MaterializedKey.sign failed: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean verifySignature(byte[] signature, byte[] originalData) throws CryptoException {
		if (this.type != KeyType.PUBLIC) {
			throw new CryptoException("verifySignature requires a PUBLIC key, got " + this.type);
		}
		try {
			PublicKey key = (PublicKey) toJdkKey();
			Signature verifier = Signature.getInstance(this.algorithm.getSignatureName(this.signatureAlgorithm));
			verifier.initVerify(key);
			verifier.update(originalData);
			return verifier.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new CryptoException("MaterializedKey.verifySignature failed: " + e.getMessage(), e);
		}
	}

	@Override
	public byte[] encrypt(byte[] clear) throws CryptoException {
		throw new CryptoException("MaterializedKey: encrypt is not supported (sign/verify only)");
	}

	@Override
	public byte[] decrypt(byte[] encoded) throws CryptoException {
		throw new CryptoException("MaterializedKey: decrypt is not supported (sign/verify only)");
	}

	@Override
	public byte[] getRawKey() {
		return this.rawBytes.clone();
	}

	@Override
	public Key getKey() throws CryptoException {
		return toJdkKey();
	}

	@Override
	public KeyType getType() {
		return this.type;
	}

	@Override
	public IKeyAlgorithm getAlgorithm() {
		return this.algorithm;
	}

	@Override
	public EncryptionMode getEncryptionMode() {
		return EncryptionMode.NONE;
	}

	@Override
	public EncryptionPaddingMode getEncryptionPaddingMode() {
		return EncryptionPaddingMode.NO_PADDING;
	}

	@Override
	public SignatureAlgorithm getSignatureAlgorithm() {
		return this.signatureAlgorithm;
	}

	private Key toJdkKey() throws CryptoException {
		try {
			KeyFactory factory = KeyFactory.getInstance(this.algorithm.getName());
			return switch (this.type) {
				case PRIVATE -> factory.generatePrivate(new PKCS8EncodedKeySpec(this.rawBytes));
				case PUBLIC -> factory.generatePublic(new X509EncodedKeySpec(this.rawBytes));
				case SECRET -> throw new CryptoException(
						"MaterializedKey does not support SECRET keys — sign/verify path only");
			};
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException("MaterializedKey: failed to reconstruct " + this.type
					+ " key for algorithm '" + this.algorithm.getName() + "': " + e.getMessage(), e);
		}
	}
}
