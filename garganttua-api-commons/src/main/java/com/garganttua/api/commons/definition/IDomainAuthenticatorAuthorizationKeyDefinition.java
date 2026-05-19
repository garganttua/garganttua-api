package com.garganttua.api.commons.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;

public interface IDomainAuthenticatorAuthorizationKeyDefinition {

	AuthenticatorKeyUsage usage();

	IKeyAlgorithm algorithm();

	SignatureAlgorithm signatureAlgorithm();

	int duration();

	TimeUnit unit();

	/**
	 * The {@link IDomainBuilder} that points at the {@code @Key}-marked
	 * entity used as the lookup-or-create storage backend. Resolved to a
	 * live domain by the runtime via {@code IApi.getDomain(domainName)} —
	 * the framework cannot keep a direct {@code IDomain} reference at
	 * build time because the key domain may itself be built later.
	 *
	 * <p>{@code null} when the user picked the supplier mode
	 * ({@code .key(supplier)}) rather than the persisted mode.
	 */
	IDomainBuilder<?> keyDomain();

	/**
	 * Whether the framework auto-creates a missing key on the configured
	 * key domain at first lookup. Default {@code true} (backward
	 * compatible). {@code false} means an absent key surfaces an
	 * {@code ApiException} at runtime — keys must be seeded out of band.
	 */
	boolean autoGenerate();

	/**
	 * Whether the framework auto-rotates a key whose persisted entry is
	 * expired or revoked. Default {@code false} (opt-in). Requires
	 * {@link #autoGenerate()} to be {@code true} (enforced at build time).
	 */
	boolean autoRotate();

}
