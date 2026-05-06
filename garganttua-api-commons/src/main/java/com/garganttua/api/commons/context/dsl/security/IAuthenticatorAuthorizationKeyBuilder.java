package com.garganttua.api.commons.context.dsl.security;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IAuthenticatorAuthorizationKeyBuilder<E> extends
                IAutomaticLinkedBuilder<IAuthenticatorAuthorizationKeyBuilder<E>, IAuthenticatorAuthorizationBuilder<E>, IAuthenticatorAuthorizationKeyContext> {

        IAuthenticatorAuthorizationKeyBuilder<E> usage(AuthenticatorKeyUsage usage);

        IAuthenticatorAuthorizationKeyBuilder<E> algorithm(IKeyAlgorithm algorithm);

        IAuthenticatorAuthorizationKeyBuilder<E> signatureAlgorithm(SignatureAlgorithm algorithm);

        IAuthenticatorAuthorizationKeyBuilder<E> lifeTime(int duration, TimeUnit unit);

}
