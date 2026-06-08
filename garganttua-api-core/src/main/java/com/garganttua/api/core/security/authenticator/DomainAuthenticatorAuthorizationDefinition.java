package com.garganttua.api.core.security.authenticator;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public record DomainAuthenticatorAuthorizationDefinition(
		int duration,
		TimeUnit unit,
		int refreshDuration,
		TimeUnit refreshUnit,
		IDomainAuthenticatorAuthorizationKeyDefinition keyDefinition,
		IDomainBuilder<?> authorizationDomainBuilder,
		ISupplierBuilder<?, ? extends ISupplier<?>> keyRealm) implements IDomainAuthenticatorAuthorizationDefinition {

}
