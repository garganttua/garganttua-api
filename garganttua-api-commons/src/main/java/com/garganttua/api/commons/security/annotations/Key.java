package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Type-level marker for an entity that represents a cryptographic key
 * (signing / verification / encryption material). Mirrors the existing
 * entity-role markers ({@code @EntityTenant}, {@code @EntityOwner},
 * {@code @Authenticator}, {@code @Authorization}).
 *
 * <p>A domain whose entity carries {@code @Key} is treated by the
 * framework as a <strong>key domain</strong>: it can be passed to
 * {@code .key(IDomainBuilder)} on an authenticator's authorization
 * builder and will then back the auto-creation / lookup of signing keys
 * driven by {@code AuthenticatorKeyUsage} (oneForAll, oneForTenant,
 * oneForEach).
 *
 * <p>An entity marked {@code @Key} is expected to also declare the
 * matching field-level annotations: {@link KeyRealmName},
 * {@link KeyAlgorithm}, {@link KeySignatureAlgorithm},
 * {@link KeyPublicMaterial}, {@link KeyPrivateMaterial},
 * {@link KeyExpiration}, {@link KeyRevoked}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Key {

}
