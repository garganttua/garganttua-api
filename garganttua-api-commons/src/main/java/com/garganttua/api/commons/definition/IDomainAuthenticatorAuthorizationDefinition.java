package com.garganttua.api.commons.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.context.dsl.IDomainBuilder;

public interface IDomainAuthenticatorAuthorizationDefinition {

	int duration();

	TimeUnit unit();

	int refreshDuration();

	TimeUnit refreshUnit();

	IDomainAuthenticatorAuthorizationKeyDefinition keyDefinition();

	IDomainBuilder<?> authorizationDomainBuilder();

}
