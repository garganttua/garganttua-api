package com.garganttua.api.commons.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.commons.security.key.KeyAlgorithm;
import com.garganttua.api.commons.security.key.SignatureAlgorithm;

public interface IDomainAuthenticatorAuthorizationKeyDefinition {

	AuthenticatorKeyUsage usage();

	KeyAlgorithm algorithm();

	SignatureAlgorithm signatureAlgorithm();

	int duration();

	TimeUnit unit();

}
