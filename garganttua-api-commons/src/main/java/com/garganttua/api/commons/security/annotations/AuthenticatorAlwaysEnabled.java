package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Type-level marker that forces the authenticator entity to be treated as
 * always-enabled. Mirrors the fluent DSL toggle
 * {@code IAuthenticatorBuilder.alwaysEnabled(true)}.
 *
 * <p>When the marker is present, the framework skips the per-row enabled /
 * accountNonLocked / accountNonExpired / credentialsNonExpired checks during
 * authentication — useful for synthetic system accounts (machine-to-machine
 * users, service principals) where these flags are irrelevant.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuthenticatorAlwaysEnabled {

}
