package com.garganttua.api.spec.engine;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;

public interface IAuthenticatorAuthorizationKeyBuilder extends
        IAutomaticLinkedBuilder<IAuthenticatorAuthorizationKeyContext, IAuthenticatorAuthorizationBuilder, IAuthenticatorAuthorizationKeyBuilder> {

    IAuthenticatorAuthorizationKeyBuilder usage(AuthenticatorKeyUsage oneforall);

    IAuthenticatorAuthorizationKeyBuilder algorithm(KeyAlgorithm hmacSha512512);

    IAuthenticatorAuthorizationKeyBuilder signatureAlgorithm(SignatureAlgorithm hmacSha512);

    IAuthenticatorAuthorizationKeyBuilder lifeTime(int i, TimeUnit days);

    IAuthenticatorAuthorizationKeyBuilder autoCreate(
            boolean b);

}
