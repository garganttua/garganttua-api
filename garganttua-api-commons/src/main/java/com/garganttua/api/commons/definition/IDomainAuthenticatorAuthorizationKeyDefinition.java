package com.garganttua.api.commons.definition;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;

public interface IDomainAuthenticatorAuthorizationKeyDefinition {

	AuthenticatorKeyUsage usage();

	IKeyAlgorithm algorithm();

	SignatureAlgorithm signatureAlgorithm();

	int duration();

	TimeUnit unit();

}
