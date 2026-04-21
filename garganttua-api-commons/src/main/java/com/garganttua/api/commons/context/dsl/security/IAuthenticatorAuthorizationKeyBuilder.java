package com.garganttua.api.commons.context.dsl.security;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.commons.security.key.KeyAlgorithm;
import com.garganttua.api.commons.security.key.SignatureAlgorithm;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IAuthenticatorAuthorizationKeyBuilder<E> extends
                IAutomaticLinkedBuilder<IAuthenticatorAuthorizationKeyBuilder<E>, IAuthenticatorAuthorizationBuilder<E>, IAuthenticatorAuthorizationKeyContext> {

        IAuthenticatorAuthorizationKeyBuilder<E> usage(AuthenticatorKeyUsage oneforall);

        IAuthenticatorAuthorizationKeyBuilder<E> algorithm(KeyAlgorithm hmacSha512512);

        IAuthenticatorAuthorizationKeyBuilder<E> signatureAlgorithm(SignatureAlgorithm hmacSha512);

        IAuthenticatorAuthorizationKeyBuilder<E> lifeTime(int i, TimeUnit days);

}
