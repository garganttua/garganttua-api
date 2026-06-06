package com.garganttua.api.commons.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IDomainAuthenticatorAuthorizationDefinition {

	int duration();

	TimeUnit unit();

	int refreshDuration();

	TimeUnit refreshUnit();

	IDomainAuthenticatorAuthorizationKeyDefinition keyDefinition();

	IDomainBuilder<?> authorizationDomainBuilder();

	/**
	 * The user-provided supplier used to obtain {@link IKeyRealm} instances at
	 * runtime for sign/verify operations. {@code null} when no {@code .keyRealm(...)}
	 * was wired on the authenticator's authorization DSL.
	 */
	ISupplierBuilder<?, ? extends ISupplier<?>> keyRealm();

}
