package com.garganttua.api.commons.definition;

import com.garganttua.core.reflection.ObjectAddress;

/**
 * Field-layout descriptor for a domain whose entity is marked
 * {@link com.garganttua.api.commons.security.annotations.Key}. Each
 * {@link ObjectAddress} points at the entity field that holds the
 * corresponding piece of key material or metadata, projecting the
 * public API of {@code com.garganttua.core.crypto.IKeyRealm} onto the
 * user's persistence shape — the user picks any entity type (including
 * {@code KeyRealm} itself) and the framework reads/writes the matching
 * fields through these addresses.
 *
 * <p>The mapping mirrors {@code IKeyRealm} method-by-method so that an
 * entity may be a {@code KeyRealm} drop-in or a custom POJO without
 * the framework having to know which:
 *
 * <ul>
 *   <li>{@link #name} — {@code IKeyRealm#getName()}</li>
 *   <li>{@link #keyAlgorithm} — {@code IKeyRealm#getKeyAlgorithm()}</li>
 *   <li>{@link #signatureAlgorithm} — {@code IKey#getSignatureAlgorithm()}
 *       (carried on the signing/verification IKey, surfaced here for storage)</li>
 *   <li>{@link #keyForSigning} — JDK PKCS#8 bytes of
 *       {@code IKeyRealm#getKeyForSigning()}</li>
 *   <li>{@link #keyForSignatureVerification} — JDK X.509 bytes of
 *       {@code IKeyRealm#getKeyForSignatureVerification()}</li>
 *   <li>{@link #keyForEncryption} — JDK-encoded bytes of
 *       {@code IKeyRealm#getKeyForEncryption()}</li>
 *   <li>{@link #keyForDecryption} — JDK-encoded bytes of
 *       {@code IKeyRealm#getKeyForDecryption()}</li>
 *   <li>{@link #expiration} — {@code IKeyRealm#getExpiration()} /
 *       {@code IKeyRealm#isExpired()}</li>
 *   <li>{@link #revoked} — {@code IKeyRealm#isRevoked()} /
 *       {@code IKeyRealm#revoke()}</li>
 *   <li>{@link #version} — {@code IKeyRealm#getVersion()}</li>
 *   <li>{@link #rotate} — last-rotation timestamp written by the framework
 *       when {@code IKeyRealm#rotate()} is invoked</li>
 * </ul>
 *
 * <p>Returned by {@code IDomainDefinition#keyDefinition()} — non-null
 * only for domains explicitly marked as key domains. Consumed at runtime
 * by the auto-create / lookup path that materializes the key for
 * signing or verification.
 */
public interface IDomainKeyDefinition {

	ObjectAddress name();

	ObjectAddress keyAlgorithm();

	ObjectAddress signatureAlgorithm();

	ObjectAddress keyForSigning();

	ObjectAddress keyForSignatureVerification();

	ObjectAddress keyForEncryption();

	ObjectAddress keyForDecryption();

	ObjectAddress expiration();

	ObjectAddress revoked();

	ObjectAddress version();

	ObjectAddress rotate();

}
