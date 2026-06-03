package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks the field on an authorization entity that holds its creation instant.
 * The framework stamps it with {@code Instant.now()} when the authorization is
 * minted (see {@code createAuthorizationEntity}). Equivalent DSL:
 * {@code .security().authorization().creation(field)}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AuthorizationCreation {

}
