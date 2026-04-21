package com.garganttua.api.core.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;

public record DomainAuthenticatorAuthorizationDefinition(
		int duration,
		TimeUnit unit,
		int refreshDuration,
		TimeUnit refreshUnit,
		IDomainAuthenticatorAuthorizationKeyDefinition keyDefinition,
		IDomainBuilder<?> authorizationDomainBuilder) implements IDomainAuthenticatorAuthorizationDefinition {

}
