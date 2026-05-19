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

        /**
         * Whether the framework auto-creates a missing key on the configured
         * key domain at first lookup. {@code true} by default (backward
         * compatible).
         *
         * <p>When {@code false}, the resolver throws if no usable key exists
         * for the scoped realmName — useful when keys must be seeded by an
         * out-of-band process (e.g. an admin import, an HSM operator) and
         * silent generation would be a bug.
         */
        IAuthenticatorAuthorizationKeyBuilder<E> autoGenerate(boolean enabled);

        /**
         * Whether the framework auto-rotates a key whose persisted entry is
         * expired or revoked. {@code false} by default (opt-in).
         *
         * <p>When {@code true}, an expired/revoked match in storage is
         * skipped, a fresh key is generated and persisted (the old entity
         * is left in place so its public material remains available for
         * verifying tokens signed before rotation). Requires
         * {@link #autoGenerate(boolean) autoGenerate=true} — rotation
         * implies generation; the build is rejected otherwise.
         *
         * <p>When {@code false} and the only candidate in storage is
         * expired/revoked, the resolver throws — the caller must rotate
         * out of band.
         */
        IAuthenticatorAuthorizationKeyBuilder<E> autoRotate(boolean enabled);

}
