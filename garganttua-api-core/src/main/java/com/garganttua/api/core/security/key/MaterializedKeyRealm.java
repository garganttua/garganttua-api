package com.garganttua.api.core.security.key;

import java.util.Date;
import java.util.Objects;

import com.garganttua.core.crypto.CryptoException;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.IKeyRealm;

/**
 * {@link IKeyRealm} reconstructed from a persisted {@code @Key} entity.
 * Owns a {@link MaterializedKey} pair (private for signing, public for
 * verification) and a small amount of envelope metadata (name,
 * algorithm, expiration, revoked flag).
 *
 * <p>Symmetric encryption / rotation are intentionally out of scope —
 * this realm exists to back the authorization-signing path. Calls to
 * {@code getKeyForEncryption}, {@code getKeyForDecryption} and
 * {@code rotate} surface {@link CryptoException} / {@link
 * UnsupportedOperationException} so misuse fails fast rather than
 * silently doing the wrong thing.
 */
public final class MaterializedKeyRealm implements IKeyRealm {

	private final String name;
	private final IKeyAlgorithm algorithm;
	private final Date expiration;
	private final MaterializedKey privateKey;
	private final MaterializedKey publicKey;
	private volatile boolean revoked;

	public MaterializedKeyRealm(String name, IKeyAlgorithm algorithm, Date expiration, boolean revoked,
			MaterializedKey privateKey, MaterializedKey publicKey) {
		this.name = Objects.requireNonNull(name, "name");
		this.algorithm = Objects.requireNonNull(algorithm, "algorithm");
		this.expiration = expiration;
		this.revoked = revoked;
		this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
		this.publicKey = Objects.requireNonNull(publicKey, "publicKey");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public IKeyAlgorithm getKeyAlgorithm() {
		return this.algorithm;
	}

	@Override
	public IKey getKeyForDecryption() throws CryptoException {
		throw new CryptoException("MaterializedKeyRealm: decryption is not supported (sign/verify only)");
	}

	@Override
	public IKey getKeyForEncryption() throws CryptoException {
		throw new CryptoException("MaterializedKeyRealm: encryption is not supported (sign/verify only)");
	}

	@Override
	public IKey getKeyForSigning() throws CryptoException {
		if (this.revoked) {
			throw new CryptoException("MaterializedKeyRealm '" + this.name + "' is revoked");
		}
		return this.privateKey;
	}

	@Override
	public IKey getKeyForSignatureVerification() throws CryptoException {
		return this.publicKey;
	}

	@Override
	public void revoke() {
		this.revoked = true;
	}

	@Override
	public boolean isRevoked() {
		return this.revoked;
	}

	@Override
	public Date getExpiration() {
		return this.expiration == null ? null : (Date) this.expiration.clone();
	}

	@Override
	public boolean isExpired() {
		return this.expiration != null && this.expiration.before(new Date());
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public IKeyRealm rotate() {
		throw new UnsupportedOperationException(
				"MaterializedKeyRealm: rotation is delegated to the @Key entity domain — "
						+ "generate a new entity and persist it through the repository, the "
						+ "next resolveKeyRealm call will pick it up.");
	}
}
