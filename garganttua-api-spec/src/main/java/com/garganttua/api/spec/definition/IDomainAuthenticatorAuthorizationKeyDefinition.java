package com.garganttua.api.spec.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;

public interface IDomainAuthenticatorAuthorizationKeyDefinition {

	AuthenticatorKeyUsage usage();

	KeyAlgorithm algorithm();

	SignatureAlgorithm signatureAlgorithm();

	int duration();

	TimeUnit unit();

}
