package com.garganttua.api.core.security.authenticator;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;

public record DomainAuthenticatorAuthorizationKeyDefinition(
		AuthenticatorKeyUsage usage,
		IKeyAlgorithm algorithm,
		SignatureAlgorithm signatureAlgorithm,
		int duration,
		TimeUnit unit,
		IDomainBuilder<?> keyDomain,
		boolean autoGenerate,
		boolean autoRotate) implements IDomainAuthenticatorAuthorizationKeyDefinition {

}
