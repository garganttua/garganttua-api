package com.garganttua.api.spec.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.context.dsl.IDomainBuilder;

public interface IDomainAuthenticatorAuthorizationDefinition {

	int duration();

	TimeUnit unit();

	int refreshDuration();

	TimeUnit refreshUnit();

	IDomainAuthenticatorAuthorizationKeyDefinition keyDefinition();

	IDomainBuilder<?> authorizationDomainBuilder();

}
