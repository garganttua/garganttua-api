package com.garganttua.api.core.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.commons.security.key.KeyAlgorithm;
import com.garganttua.api.commons.security.key.SignatureAlgorithm;

public record DomainAuthenticatorAuthorizationKeyDefinition(
		AuthenticatorKeyUsage usage,
		KeyAlgorithm algorithm,
		SignatureAlgorithm signatureAlgorithm,
		int duration,
		TimeUnit unit) implements IDomainAuthenticatorAuthorizationKeyDefinition {

}
