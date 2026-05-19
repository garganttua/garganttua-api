package com.garganttua.api.commons.definition;

import com.garganttua.core.reflection.ObjectAddress;

/**
 * Field-layout descriptor for a domain whose entity is marked
 * {@link com.garganttua.api.commons.security.annotations.Key}. Each
 * {@link ObjectAddress} points at the entity field that holds the
 * corresponding piece of key material or metadata.
 *
 * <p>Returned by {@link IDomainDefinition#keyDefinition()} — non-null
 * only for domains explicitly marked as key domains. Consumed at runtime
 * by the auto-create / lookup path that materializes the key for
 * signing or verification.
 */
public interface IDomainKeyDefinition {

	ObjectAddress realmName();

	ObjectAddress algorithm();

	ObjectAddress signatureAlgorithm();

	ObjectAddress publicMaterial();

	ObjectAddress privateMaterial();

	ObjectAddress expiration();

	ObjectAddress revoked();

}
